package uk.gov.ons.census.fwmt.tests.acceptance.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.data.dto.comet.HouseholdOutcome;
import uk.gov.ons.census.fwmt.data.dto.rm.OutcomeEvent;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class CensusSteps {

  private static final String RM_REQUEST_RECEIVED = "RM - Request Received";
  private static final String COMET_CREATE_JOB_REQUEST = "Comet - Create Job Request";
  private String receivedRMMessage = null;
  private String invalidRMMessage = null;
  private String testOutcomeJson = null;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueUtils queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    receivedRMMessage = Resources.toString(Resources.getResource("files/input/actionInstruction.xml"), Charsets.UTF_8);
    invalidRMMessage = Resources.toString(Resources.getResource("files/input/invalidInstruction.xml"), Charsets.UTF_8);
    testOutcomeJson = null;

    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
  }

  @Given("a TM doesnt have an existing job with case ID {string}")
  public void aTMDoesntHaveAnExistingJobWithCaseId(String caseId) {
    try {
      tmMockUtils.getCaseById(caseId);
      fail("Case should not exist");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @And("RM sends a create HouseHold job request with case ID of {string}")
  public void rmSendsACreateHouseHoldJobRequestWithCaseIDOf(String caseId)
      throws URISyntaxException, InterruptedException {
    queueUtils.sendToActionFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM with case ID of {string}")
  public void theGatewaySendsACreateJobMessageToTMWithCaseIdOf(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_JOB_REQUEST, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with ID of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
  }

  @Given("TM sends a {string} Census Case Outcome to the Gateway")
  public void tmSendsACensusCaseOutcomeToTheGateway(String outcomeType) throws IOException {
    switch (outcomeType) {
    case "derelict":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/noValidHouseHoldDerelict.txt"), Charsets.UTF_8);
      break;
    case "splitAddress":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/contactMadeSplitAddress.txt"), Charsets.UTF_8);
      break;
    case "hardRefusal":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/contactMadeHardRefusal.txt"), Charsets.UTF_8);
      break;
    }

    int response = tmMockUtils.sendTMResponseMessage(testOutcomeJson);
    assertEquals(200, response);
  }

  @And("the response is of a Census Case Outcome format")
  public void theResponseIsOfACensusCaseOutcomeFormat() {
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
        DateTimeFormatter.ISO_DATE_TIME);
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
    objectMapper = Jackson2ObjectMapperBuilder.json()
        .modules(module)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    try {
      objectMapper.readValue(testOutcomeJson.getBytes(), HouseholdOutcome.class);
    } catch (IOException e) {
      fail();
    }
  }

  @And("the response contains the Primary Outcome value of {string} and Secondary Outcome {string} and the Case Id of {string}")
  public void theResponseContainsThePrimaryOutcomeValueOfSecondaryOutcomeAndTheCaseIdOf(String primaryOutcome,
      String secondaryOutcome,
      String caseId) throws IOException {
    HouseholdOutcome householdOutcome = objectMapper.readValue(testOutcomeJson.getBytes(), HouseholdOutcome.class);
    assertEquals(primaryOutcome, householdOutcome.getPrimaryOutcome());
    assertEquals(secondaryOutcome, householdOutcome.getSecondaryOutcome());
    assertEquals(caseId, String.valueOf(householdOutcome.getCaseId()));
  }

  @Then("the message will made available for RM to pick up from queue {string}")
  public void theMessageWillMadeAvailableForRMToPickUpFromQueue(String queueName) {
    assertEquals(1, queueUtils.getMessageCount(queueName));
  }

  @And("the message is in the format RM is expecting from queue {string}")
  public void theMessageIsInTheFormatRMIsExpectingFromQueue(String queueName) {
    try {
      objectMapper.readValue(queueUtils.getMessage(queueName), OutcomeEvent.class);
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  // Unused steps - should I delete?
  @Given("a message in an invalid format from RM")
  public void aMessageInAnInvalidFormatFromRm() throws URISyntaxException, InterruptedException {
    queueUtils.sendToActionFieldQueue(invalidRMMessage);
    throw new cucumber.api.PendingException();
  }

  @Given("a message in an invalid format from TM")
  public void aMessageInAnInvalidFormatFromTm() {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  @Given("a message received from RM that fails to send to TM after {int} attempts")
  public void aMessageReceivedFromRmThatFailsToSendToTmAfterAttempts(Integer attempts) {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  // Shared steps
  @Then("the error is logged via SPLUNK & stored in a queue {string}")
  public void theErrorIsLoggedViaSPLUNKStoredInA(String queueName) {
    assertEquals(1, queueUtils.getMessageCount(queueName));
  }
}
