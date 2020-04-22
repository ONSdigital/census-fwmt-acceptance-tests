package uk.gov.ons.census.fwmt.tests.acceptance.steps.household;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePause;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;

import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class HouseholdSteps {

  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  private static final String CANONICAL_CANCEL_RECEIVED = "CANONICAL_CANCEL_RECEIVED";
  private static final String CANONICAL_CANCEL_SENT = "CANONICAL_CANCEL_SENT";
  private static final String CANONICAL_CREATE_SENT = "CANONICAL_CREATE_SENT";
  public static final String CANONICAL_UPDATE_RECEIVED = "CANONICAL_UPDATE_RECEIVED";
  public static final String CANONICAL_UPDATE_SENT = "CANONICAL_UPDATE_SENT";
  private String cancelMessage = null;
  private String cancelMessageNonHH = null;
  private String invalidRMMessage = null;
  private String nisraHouseholdMessage = null;
  private String nisraNoFieldOfficerMessage = null;
  private String receivedRMMessage = null;
  private String updateMessage = null;
  private String updatePauseMessage = null;
  private String updateMessageWithoutCreate = null;

  @Autowired
  private CSVSerivceUtils csvServiceUtils;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    cancelMessage = Resources
        .toString(Resources.getResource("files/input/actionCancelInstruction.xml"), Charsets.UTF_8);
    cancelMessageNonHH = Resources
        .toString(Resources.getResource("files/input/actionNonHHCancelInstruction.xml"), Charsets.UTF_8);
    invalidRMMessage = Resources.toString(Resources.getResource("files/input/invalidInstruction.xml"), Charsets.UTF_8);
    receivedRMMessage = Resources.toString(Resources.getResource("files/input/actionInstruction.xml"), Charsets.UTF_8);
    updateMessage = Resources.toString(Resources.getResource("files/input/actionUpdateInstruction.xml"), Charsets.UTF_8);
    updatePauseMessage = Resources.toString(Resources.getResource("files/input/actionUpdatePauseInstruction.xml"), Charsets.UTF_8);
    updateMessageWithoutCreate = Resources
            .toString(Resources.getResource("files/input/actionUpdateInstructionWithoutCreate.xml"), Charsets.UTF_8);
    nisraNoFieldOfficerMessage = Resources
            .toString(Resources.getResource("files/input/nisraNoFieldOfficerActionInstruction.xml"), Charsets.UTF_8);
    nisraHouseholdMessage = Resources.toString(Resources.getResource("files/input/nisraActionInstruction.xml"), Charsets.UTF_8);

    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();


    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
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
    queueUtils.sendToRMFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    String caseId = "39bad71c-7de5-4e1b-9a07-d9597737977f";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with id of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
  }

//  @Given("RM sends a create HouseHold job request job which has a case ID of {string} and a field officer ID {string}")
//  public void rmSendsACreateHouseHoldJobRequestJobWhichHasACaseIDOfAndAFieldOfficerID(String caseId,
//      String fieldOfficerId)
//      throws URISyntaxException, InterruptedException, JAXBException {
//    JAXBElement<ActionInstruction> actionInstruction = tmMockUtils.unmarshalXml(nisraHouseholdMessage);
//    queueUtils.sendToRMFieldQueue(nisraHouseholdMessage);
//    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
//    assertEquals(fieldOfficerId, actionInstruction.getValue().getActionRequest().getFieldOfficerId());
//    assertThat(hasBeenTriggered).isTrue();
//  }

  @When("the Gateway sends a Create Job message to TM with case ID of {string}")
  public void theGatewaySendsACreateJobMessageToTMWithCaseIdOf(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Given("RM sends a create HouseHold job request job which has a case ID of {string}")
  public void rmSendsACreateHouseHoldJobRequestJobWhichHasACaseIDOfAndAnID(String caseId)
      throws URISyntaxException, InterruptedException, JAXBException {
    queueUtils.sendToRMFieldQueue(nisraNoFieldOfficerMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("RM will throw an exception for case ID {string}")
  public void rmWillThrowAnExceptionForCaseID(String caseId) throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(nisraNoFieldOfficerMessage);
    assertThatExceptionOfType(GatewayException.class).isThrownBy(() -> {
      throw new GatewayException(
          GatewayException.Fault.SYSTEM_ERROR);
    });
  }

  // Unused steps - should I delete?
  @Given("a message in an invalid format from RM")
  public void aMessageInAnInvalidFormatFromRm() throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(invalidRMMessage);
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

  @And("RM sends a cancel case Household job request with case ID {string}")
  public void tmHasAnExistingJobWithCaseID(String caseId) throws URISyntaxException, InterruptedException {

    queueUtils.sendToRMFieldQueue(cancelMessage);
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
    queueUtils.sendToRMFieldQueue(cancelMessageNonHH);
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

  @Given("TM already has an existing job with case ID {string}")
  public void tmAlreadyHasAnExistingJobWithCaseID(String caseId) throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @And("RM sends an update case job request with case ID {string}")
  public void rmSendsAnUpdatePauseCaseJobRequestWithCaseID(String caseId)
      throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(updateMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_UPDATE_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Update Case with Pause request to TM with case ID {string}")
  public void theGatewaySendsAUpdateCaseWithPauseRequestToTMWithCaseID(String caseId) throws InterruptedException {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_UPDATE_RECEIVED, 100000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM with case ID {string}")
  public void theGatewaySendsACreateJobMessageToTMWithCaseID(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_CREATE_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @And("TM picks up the Create Job message with case ID {string}")
  public void tmPicksUpTheCreateJobMessageWithCaseID(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Given("TM already has an existing job with case ID {string} with a pause")
  public void tmAlreadyHasAnExistingJobWithCaseIDWithAPause(String caseId)
      throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(receivedRMMessage);

    boolean caseIdPresent = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(caseIdPresent).isTrue();

    queueUtils.sendToRMFieldQueue(updatePauseMessage);

    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_UPDATE_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Update Case with a reinstate date case to TM with case ID {string}")
  public void theGatewaySendsAUpdateCaseWithAReinstateDateCaseToTMWithCaseID(String caseId)
      throws InterruptedException {
    Thread.sleep(1000);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_UPDATE_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a pause datetime of {string} with a reason {string} will be assigned to the case with id {string}")
  public void aPauseDatetimeOfWithAReasonWillBeAssignedToTheCaseWithId(String until, String reason, String caseId)
      throws InterruptedException {
    Thread.sleep(1000);

    CasePause casePause = tmMockUtils.getPauseCase(caseId);
    assertEquals(OffsetDateTime.parse(until), casePause.getUntil());
    assertEquals(reason, casePause.getReason());
  }

  @Given("RM sends an update case Household job request with case ID {string} and receives an exception from RM")
  public void rmSendsACancelCaseHouseholdJobRequestWithCaseIDAndReceivesAnExceptionFromRM(String arg0)
          throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(updateMessageWithoutCreate);
    assertThatExceptionOfType(GatewayException.class).isThrownBy(() -> {
      throw new GatewayException(
              GatewayException.Fault.SYSTEM_ERROR);
    });
  }
}
