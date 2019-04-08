package uk.gov.ons.census.fwmt.tests.acceptance.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.data.dto.comet.HouseholdOutcome;
import uk.gov.ons.census.fwmt.data.dto.rm.OutcomeEvent;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class CensusSteps {

  private String noValidHouseholdDerelict = null;
  private static final String RM_REQUEST_RECEIVED = "RM - Request Received";
  private static final String COMET_CREATE_JOB_REQUEST = "Comet - Create Job Request";
  private String receivedRMMessage = null;
  private String invalidRMMessage = null;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueUtils queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmURL;
  @Value("${service.rabbit.url}")
  private String rabbitLocation;
  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void reset() throws IOException, TimeoutException, URISyntaxException {
    noValidHouseholdDerelict = Resources.toString(Resources.getResource("files/cometNoValidHouseHoldDerelict.txt"), Charsets.UTF_8);
    receivedRMMessage = Resources.toString(Resources.getResource("files/actionInstruction.xml"), Charsets.UTF_8);
    invalidRMMessage = Resources.toString(Resources.getResource("files/invalidInstruction.xml"), Charsets.UTF_8);

    tmMockUtils.enableCaseManager();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableCaseManager();
  }

  @Given("a TM doesnt have an existing job with id {string}")
  public void aTMDoesntHaveAnExistingJobWithId(String id) {
    try {
      tmMockUtils.getCaseById(id);
      fail("Case should not exist");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @And("RM sends a create HouseHold job request")
  public void rmSendsACreateHouseHoldJobRequest() throws URISyntaxException, InterruptedException {
    String caseID = "39bad71c-7de5-4e1b-9a07-d9597737977f";
    queueUtils.sendToActionFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseID, RM_REQUEST_RECEIVED);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    String caseID = "39bad71c-7de5-4e1b-9a07-d9597737977f";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseID, COMET_CREATE_JOB_REQUEST);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with id of {string} is created in TM")
  public void a_new_case_is_created_in_TM(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase kase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, kase.getId().toString());
  }

  @Given("a message in an invalid format from RM")
  public void a_message_in_an_invalid_format_from_RM() throws URISyntaxException, InterruptedException {
    queueUtils.sendToActionFieldQueue(invalidRMMessage);
  }

  @Given("a message in an invalid format from TM")
  public void a_message_in_an_invalid_format_from_TM() {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  @Given("a message received from RM that fails to send to TM after {int} attempts")
  public void a_message_received_from_RM_that_fails_to_send_to_TM_after_attempts(Integer attempts) {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  @Given("TM sends a Census Case Outcome to the Gateway")
  public void tmSendsACensusCaseOutcomeToTheGateway() {
    int response = tmMockUtils.sendTMResponseMessage(noValidHouseholdDerelict);
    assertEquals(200, response);
  }

  @And("the response is of a Census Case Outcome format")
  public void theResponseIsOfACensusCaseOutcomeFormat() {
    try {
      objectMapper.readValue(noValidHouseholdDerelict.getBytes(), HouseholdOutcome.class);
    } catch (IOException e) {
      fail();
    }
  }

  @And("the response contains the Primary Outcome value of {string}, Secondary Outcome {string} and the Case Id of {string}")
  public void theResponseContainsThePrimaryOutcomeValueOfSecondaryOutcomeAndTheCaseIdOf(String primaryoutcome, String secondaryOutcome,
      String caseId) throws IOException {
    HouseholdOutcome householdOutcome = objectMapper.readValue(noValidHouseholdDerelict.getBytes(), HouseholdOutcome.class);
    assertEquals(primaryoutcome, householdOutcome.getPrimaryOutcome());
    assertEquals(secondaryOutcome, householdOutcome.getSecondaryOutcome());
    assertEquals(caseId, householdOutcome.getCaseId());
  }

  @Then("the message will made available for RM to pick up")
  public void theMessageWillMadeAvailableForRMToPickUp() {
    assertEquals(1, queueUtils.getMessageCount("Gateway.Outcome"));
  }

  @And("the message is in the format RM is expecting")
  public void theMessageIsInTheFormatRMIsExpecting() {
    try {
      objectMapper.readValue(queueUtils.getMessage("Gateway.Outcome"), OutcomeEvent.class);
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  // Shared step
  @Then("the error is logged via SPLUNK & stored in a queue {string}")
  public void theErrorIsLoggedViaSPLUNKStoredInA(String queueName) {
    assertEquals(1, queueUtils.getMessageCount(queueName));
  }

}
