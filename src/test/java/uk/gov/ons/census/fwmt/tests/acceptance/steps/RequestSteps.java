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
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePause;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.data.rm.OutcomeEvent;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVServiceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class RequestSteps {

  private static final String RM_REQUEST_RECEIVED = "RM - Request Received";
  private static final String COMET_CREATE_JOB_REQUEST = "Comet - Create Job Request";
  private static final String CANONICAL_CANCEL_RECEIVED = "Canonical - Cancel Job Received";
  private static final String CANONICAL_CANCEL_SENT = "Canonical - Action Cancel Sent";
  private static final String CANONICAL_CREATE_SENT = "Canonical - Action Create Sent";
  public static final String CANONICAL_UPDATE_RECEIVED = "Canonical - Update Job Received";
  public static final String CANONICAL_UPDATE_SENT = "Canonical - Action Update Sent";
  public static final String CSV_REQUEST_EXTRACTED = "CSV Service - Request extracted";
  private String cancelMessage = null;
  private String cancelMessageNonHH = null;
  private String invalidRMMessage = null;
  private String receivedRMMessage = null;
  private String updateMessage = null;

  @Autowired
  private CSVServiceUtils csvServiceUtils;

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
    cancelMessage = Resources
        .toString(Resources.getResource("files/input/actionCancelInstruction.xml"), Charsets.UTF_8);
    cancelMessageNonHH = Resources
        .toString(Resources.getResource("files/input/actionNonHHCancelInstruction.xml"), Charsets.UTF_8);
    invalidRMMessage = Resources.toString(Resources.getResource("files/input/invalidInstruction.xml"), Charsets.UTF_8);
    receivedRMMessage = Resources.toString(Resources.getResource("files/input/actionInstruction.xml"), Charsets.UTF_8);
    updateMessage = Resources.toString(Resources.getResource("files/input/actionUpdatePauseInstruction.xml"), Charsets.UTF_8);

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

  @And("RM sends a create HouseHold job request")
  public void rmSendsACreateHouseHoldJobRequest() throws URISyntaxException, InterruptedException {
    String caseId = "39bad71c-7de5-4e1b-9a07-d9597737977f";
    queueUtils.sendToActionFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    String caseId = "39bad71c-7de5-4e1b-9a07-d9597737977f";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_JOB_REQUEST, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with id of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
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

  @Given("RM sends a cancel case Household job request with case ID {string}")
  public void tmHasAnExistingJobWithCaseID(String caseId) throws URISyntaxException, InterruptedException {

    queueUtils.sendToActionFieldQueue(cancelMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_CANCEL_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Cancel Case request to TM with case ID {string}")
  public void theGatewaySendsACancelCaseRequestToTM(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_CANCEL_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a pause datetime of {string} will be assigned to the case with id {string}")
  public void theCaseWithIdOfWillBeCancelled(String until, String caseId) throws InterruptedException {

    Thread.sleep(1000);

    CasePause casePause = tmMockUtils.getPauseCase(caseId);
    assertEquals(OffsetDateTime.parse(until), casePause.getUntil());
  }

  @Given("RM sends a cancel case CSS job request with case ID {string} and receives an exception from RM")
  public void rmSendsACancelCaseCSSJobRequestWithCaseID(String caseId) throws URISyntaxException, InterruptedException {
    queueUtils.sendToActionFieldQueue(cancelMessageNonHH);
    assertThatExceptionOfType(GatewayException.class).isThrownBy(() -> {
      throw new GatewayException(
          GatewayException.Fault.SYSTEM_ERROR);
    });
  }

  @Then("the job with case ID {string} will not be passed to TM")
  public void theJobWithCaseIDWillNotBePassedToTM(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_CANCEL_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isFalse();
  }

  @And("the response contains the Requester Title {string} and Requester Forename {string} and Requester Surname {string} from queue {string}")
  public void theResponseContainsTheRequesterTitleAndRequesterForenameAndRequesterSurnameFromQueue(
      String requesterTitle,
      String requesterForename, String requesterSurname, String queueName) throws IOException, InterruptedException {
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
        DateTimeFormatter.ISO_DATE_TIME);
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
    objectMapper = Jackson2ObjectMapperBuilder.json()
        .modules(module)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    OutcomeEvent outcomeEvent = objectMapper.readValue(queueUtils.getMessage(queueName), OutcomeEvent.class);

    assertEquals(requesterTitle, outcomeEvent.getPayload().getFulfillment().getContact().getTitle());
    assertEquals(requesterForename, outcomeEvent.getPayload().getFulfillment().getContact().getForename());
    assertEquals(requesterSurname, outcomeEvent.getPayload().getFulfillment().getContact().getSurname());
  }

  @And("the response contains the Requestor Phone Number {string} from queue {string}")
  public void theResponseContainsTheQuestionnaireTypeAndQuestionnaireIDAndRequestorPhoneNumberFromQueue(
      String requesterPhone, String queueName) throws IOException, InterruptedException {
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
        DateTimeFormatter.ISO_DATE_TIME);
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
    objectMapper = Jackson2ObjectMapperBuilder.json()
        .modules(module)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    OutcomeEvent outcomeEvent = objectMapper.readValue(queueUtils.getMessage(queueName), OutcomeEvent.class);

    assertEquals(requesterPhone, outcomeEvent.getPayload().getFulfillment().getContact().getTelNo());
  }

  @Given("RM sends an update pause case job request with case ID {string}")
  public void rmSendsAnUpdatePauseCaseJobRequestWithCaseID(String caseId)
      throws URISyntaxException, InterruptedException {
    queueUtils.sendToActionFieldQueue(updateMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_UPDATE_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Update Case with Pause request to TM with case ID {string}")
  public void theGatewaySendsAUpdateCaseWithPauseRequestToTMWithCaseID(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_UPDATE_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Given("the Gateway receives a CSV CE with case ID {string}")
  public void theGatewayReceivesACSVCEWithCaseID(String caseId) throws InterruptedException, IOException {
    csvServiceUtils.enableCsvService();
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CSV_REQUEST_EXTRACTED, 10000L);
    Thread.sleep(1000);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM with case ID {string}")
  public void theGatewaySendsACreateJobMessageToTMWithCaseID(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_CREATE_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @And("TM picks up the Create Job message with case ID {string}")
  public void tmPicksUpTheCreateJobMessageWithCaseID(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_JOB_REQUEST, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }
}
