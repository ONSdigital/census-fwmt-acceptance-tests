package uk.gov.ons.census.fwmt.tests.acceptance.steps.spg.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static uk.gov.ons.census.fwmt.tests.acceptance.steps.spg.inbound.SPGCommonUtils.testBucket;

import java.net.URISyntaxException;

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
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;

@Slf4j
public class SPGUpdateSteps {

  @Autowired
  private SPGCommonUtils spgCommonUtils;

  @Autowired
  private QueueClient queueUtils;

  @Autowired
  private GatewayEventMonitor gatewayEventMonitor;

  private static final String RM_UPDATE_REQUEST_RECEIVED = "RM_UPDATE_REQUEST_RECEIVED";

  private static final String COMET_UPDATE_PRE_SENDING = "COMET_UPDATE_PRE_SENDING";

  private static final String COMET_UPDATE_ACK = "COMET_UPDATE_ACK";

  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

  private static final String ROUTING_FAILED = "ROUTING_FAILED";

  private static final String CONVERT_SPG_UNIT_UPDATE_TO_CREATE = "CONVERT_SPG_UNIT_UPDATE_TO_CREATE";

  private static final String COMET_CREATE_PRE_SENDING = "COMET_CREATE_PRE_SENDING";

  private String ceSpgEstabUpdateJson = null;

  private String ceSpgUnitUpdateJson = null;

  private String ceSpgEstabCreateJson = null;

  private String ceSpgUnitCreateJson = null;

  private GatewayEventDTO event_COMET_UPDATE_ACK;

  @Before
  public void setup() throws Exception {
    ceSpgEstabCreateJson = Resources.toString(Resources.getResource("files/input/spg/spgEstabCreate.json"), Charsets.UTF_8);
    ceSpgUnitCreateJson = Resources.toString(Resources.getResource("files/input/spg/spgUnitCreate.json"), Charsets.UTF_8);

    ceSpgEstabUpdateJson = Resources.toString(Resources.getResource("files/input/spg/spgEstabUpdate.json"), Charsets.UTF_8);
    ceSpgUnitUpdateJson = Resources.toString(Resources.getResource("files/input/spg/spgUnitUpdate.json"), Charsets.UTF_8);
    spgCommonUtils.setup();
  }

  @After
  public void clearDown() throws Exception {
    spgCommonUtils.clearDown();
  }

  @And("RM sends an update case request for the case")
  public void rmSendsUpdate() throws URISyntaxException {
    String caseId = testBucket.get("caseId");
    String type = testBucket.get("type");

    JSONObject json = new JSONObject(getUpdateRMJson());
    json.remove("caseId");
    json.put("caseId", caseId);

    json.remove("addressLevel");
    if ("Estab".equals(type)) {
      json.put("addressLevel", "E");
    }
    if ("Unit".equals(type)) {
      json.put("addressLevel", "U");
    }
    String request = json.toString(4);
    log.info("Request = " + request);
    queueUtils.sendToRMFieldQueue(request, "update");
  }

  @When("Gateway receives an update message for the case")
  public void gatewayReceivesTheMessage() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_UPDATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("it will update the job in TM")
  public void confirmTmAction() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_UPDATE_PRE_SENDING, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @And("the updated job is acknowledged by TM")
  public void the_cancel_job_is_acknowledged_by_tm() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_UPDATE_ACK, 10000L);
    assertTrue(hasBeenTriggered);
  }
  
  @Given("RM sends a unit update case request where undeliveredAsAddress is {string}")
  public void rm_sends_a_cancel_case_request(String undeliveredAsAddress) throws URISyntaxException {
    JSONObject json = new JSONObject(ceSpgUnitUpdateJson);
    json.remove("undeliveredAsAddress");
    json.put("undeliveredAsAddress", "true".equals(undeliveredAsAddress));

    String request = json.toString(4);
    log.info("Request = " + request);
    queueUtils.sendToRMFieldQueue(request, "update");
  }

  @Then("the update job should fail")
  public void the_update_job_should_fail_by_tm() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasErrorEventTriggered(caseId, ROUTING_FAILED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("Gateway will reroute it as a create message")
  public void gateway_will_reroute_it_as_a_create_message() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CONVERT_SPG_UNIT_UPDATE_TO_CREATE, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("Gateway will send a create job to TM")
  public void gateway_will_send_a_create_job_to_TM() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_PRE_SENDING, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("the create job is acknowledged by tm")
  public void the_create_job_is_acknowledged_by_tm() {
    String caseId = testBucket.get("caseId");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  private String getUpdateRMJson() {
    String survey = testBucket.get("survey");
    String type = testBucket.get("type");
    switch (type) {
    case "Estab":
      return ceSpgEstabUpdateJson;
    case "Unit":
      return ceSpgUnitUpdateJson;
    default:
      throw new RuntimeException("Incorrect survey " + survey + " and type " + type);
    }
  }

}
