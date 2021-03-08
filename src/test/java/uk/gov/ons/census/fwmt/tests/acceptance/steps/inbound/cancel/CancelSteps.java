package uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.cancel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common.CommonUtils.testBucket;

import java.net.URISyntaxException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common.CommonUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;

@Slf4j
public class CancelSteps {

  @Autowired
  private CommonUtils commonUtils;

  @Autowired
  private GatewayEventMonitor gatewayEventMonitor;

  private GatewayEventDTO event_COMET_CANCEL_PRE_SENDING;

  private String spgCancel;
  private String ceEstabCancel;
  private String ceUnitCancel;
  private String hhCancel;
  private String ncHhCancel;
  private String ncCeCancel;
  private String ccsIntCeCancel;
  private String ccsIntHhCancel;
  private String feddbackCeCancel;

  private static final String RM_CANCEL_REQUEST_RECEIVED = "RM_CANCEL_REQUEST_RECEIVED";

  private static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";

  private static final String ROUTING_FAILED = "ROUTING_FAILED";

  private static final String COMET_CANCEL_PRE_SENDING = "COMET_CANCEL_PRE_SENDING";


  @Autowired
  private QueueClient queueClient;

  @Before
  public void setup() throws Exception {
    spgCancel = Resources.toString(Resources.getResource("files/input/spg/spgCancel.json"), Charsets.UTF_8);
    ceEstabCancel = Resources.toString(Resources.getResource("files/input/ce/ceEstabCancel.json"), Charsets.UTF_8);
    ceUnitCancel = Resources.toString(Resources.getResource("files/input/ce/ceUnitCancel.json"), Charsets.UTF_8);
    hhCancel = Resources.toString(Resources.getResource("files/input/hh/hhCancel.json"), Charsets.UTF_8);
    ncCeCancel = Resources.toString(Resources.getResource("files/input/ce/ncCeEstabCancel.json"), Charsets.UTF_8);
    ncHhCancel = Resources.toString(Resources.getResource("files/input/hh/ncHhCancel.json"), Charsets.UTF_8);
    ccsIntHhCancel = Resources.toString(Resources.getResource("files/input/ccsint/ccsIntHhCancel.json"), Charsets.UTF_8);
    ccsIntCeCancel = Resources.toString(Resources.getResource("files/input/ccsint/ccsIntCeCancel.json"), Charsets.UTF_8);
    feddbackCeCancel = Resources.toString(Resources.getResource("files/input/ce/ceUnitCancel.json"), Charsets.UTF_8);
    // commonUtils.setup(); // Leaving this commented out to highlight that this should not be uncommented
    // as its already done by the createSteps.setup(). And doing it twice has some strange results
  }

  @After
  public void clearDown() throws Exception {
//    commonUtils.clearDown();// Leaving this commented out to highlight that this should not be uncommented
    // as its already done by the createSteps.setup(). And doing it twice has some strange results
  }

  @Then("RM sends a cancel case request for the case")
  public void rmSendsCancel() throws URISyntaxException {
    String caseId = testBucket.get("caseId");
    String type = testBucket.get("type");

    JSONObject json = new JSONObject(getCreateRMJson());

    json.remove("caseId");
    json.put("caseId", caseId);

    json.remove("addressLevel");
    if ("Estab".equals(type) || "CE Est".equals(type) || "CE Site".equals(type) || "NC CE".equals(type) || "CCS Int CE".equals(type)) {
      json.put("addressLevel", "E");
    }
    if ("Unit".equals(type) || "CE Unit".equals(type) || "E&W".equals(type) || "NC HH".equals(type)|| "CCS Int HH".equals(type)) {
      json.put("addressLevel", "U");
    }

    String request = json.toString(4);
    log.info("Request = " + request);
    queueClient.sendToRMFieldQueue(request, "cancel");
  }

  @Then("RM sends a NC cancel case request for the case with caseId {string}")
  public void rmSendsNCCancel(String caseId) throws URISyntaxException {
    testBucket.put("caseId", caseId);
    String type = testBucket.get("type");

    JSONObject json = new JSONObject(getCreateRMJson());

    json.remove("addressLevel");
    if ("Estab".equals(type) || "CE Est".equals(type) || "CE Site".equals(type) || "NC CE".equals(type)) {
      json.put("addressLevel", "E");
    }
    if ("Unit".equals(type) || "CE Unit".equals(type) || "E&W".equals(type) || "NC HH".equals(type)) {
      json.put("addressLevel", "U");
    }

    String request = json.toString(4);
    log.info("Request = " + request);
    queueClient.sendToRMFieldQueue(request, "cancel");
  }

  @Then("RM sends a feedback cancel case request for the case")
  public void rmSendsFeedbackCancel() throws URISyntaxException {
    JSONObject json = new JSONObject(feddbackCeCancel);

    String request = json.toString(4);
    log.info("Request = " + request);
    queueClient.sendToRMFieldQueue(request, "cancel");
  }

  @When("Gateway receives a cancel message for the case")
  public void gatewayReceivesTheMessage() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CANCEL_REQUEST_RECEIVED, CommonUtils.TIMEOUT);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("Gateway receives a NC cancel message for the case for caseId {string}")
  public void gatewayReceivesTheNCMessage(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CANCEL_REQUEST_RECEIVED, CommonUtils.TIMEOUT);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("it will Cancel the job with with the correct TM Action {string}")
  public void confirmTmAction(String expectedTmAction) {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CANCEL_PRE_SENDING, CommonUtils.TIMEOUT);
    assertThat(hasBeenTriggered).isTrue();
    List<GatewayEventDTO> events = gatewayEventMonitor.getEventsForEventType(COMET_CANCEL_PRE_SENDING, 1);
    event_COMET_CANCEL_PRE_SENDING = events.get(0);
    String actualTmAction = event_COMET_CANCEL_PRE_SENDING.getMetadata().get("TM Action");
    assertEquals("TM Actions created for TM", expectedTmAction, actualTmAction);
  }

  @Then("the cancel job is acknowledged by TM")
  public void the_cancel_job_is_acknowledged_by_tm() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CANCEL_ACK, CommonUtils.TIMEOUT);
    assertTrue(hasBeenTriggered);
  }

  @Then("the NC cancel job for {string} is acknowledged by TM")
  public void the_cancel_job_is_acknowledged_by_tm(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CANCEL_ACK, CommonUtils.TIMEOUT);
    assertTrue(hasBeenTriggered);
  }

  @Then("the cancel job should fail")
  public void the_cancel_job_should_fail() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasErrorEventTriggered(caseId, ROUTING_FAILED, CommonUtils.TIMEOUT);
    assertTrue(hasBeenTriggered);
  }

  @Given("RM sends a cancel case request")
  public void rm_sends_a_cancel_case_request() throws URISyntaxException {
    JSONObject json = new JSONObject(spgCancel);
    json.remove("addressLevel");
    json.put("addressLevel", "E");


    String request = json.toString(4);
    log.info("Request = " + request);
    queueClient.sendToRMFieldQueue(request, "cancel");
  }

  private String getCreateRMJson() {
    String type = testBucket.get("type");
    String survey = testBucket.get("survey");

    if (survey.equals("HH") && !type.equals("NC HH"))
      return hhCancel;

    switch (type) {
      case "Estab":
      case "Unit":
        return spgCancel;
      case "CE Est":
      case "CE Site":
        return ceEstabCancel;
      case "CE Unit":
        return ceUnitCancel;
      case "CCS Int CE":
        return ccsIntCeCancel;
      case "CCS Int HH":
        return ccsIntHhCancel;
      case "NC CE":
        return ncCeCancel;
      case "NC HH":
        return ncHhCancel;
      default:
        throw new RuntimeException("Incorrect survey " + survey + " and type " + type);
    }
  }
}
