package uk.gov.ons.census.fwmt.tests.acceptance.steps.spg.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

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

  private String survey = null;

  private String type = null;

  private String caseId = null;

  private String caseRef = null;

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

  @Given("a job has been created a {string} {string} job in TM with case id {string} with caseRef {string}")
  public void aJobHasBeenCreatedAJobInTMWithCaseId(String survey, String type, String caseId, String caseRef)
      throws URISyntaxException {
        
    this.survey = survey;
    this.type = type;

    this.caseId = caseId;
    this.caseRef = caseRef;

    JSONObject json = new JSONObject(getCreateRMJson());
    json.remove("caseId");
    json.put("caseId", caseId);

    json.remove("caseRef");
    json.put("caseRef", caseRef);

    String request = json.toString(4);
    log.info("Resquest = " + request);
    
    queueUtils.sendToRMFieldQueue(request, "create");
    boolean jobAcknowledged = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(jobAcknowledged).isTrue();
  }


  @Given("RM sends a update SPG Unit job request as undeliveredAsAddress {string} and case id {string} with caseRef {string}")
  public void rm_sends_a_update_SPG_job_request_as_undeliveredAsAddress(String undeliveredAsAddress, String caseId, String caseRef) throws URISyntaxException {
    this.caseId = caseId;
    this.caseRef = caseRef;
    type = "Unit";
    
    JSONObject json = new JSONObject(getUpdateRMJson());
    json.remove("caseId");
    json.put("caseId", caseId);

    json.remove("caseRef");
    json.put("caseRef", caseRef);

    json.remove("undeliveredAsAddress");
    json.put("undeliveredAsAddress", undeliveredAsAddress);

    String request = json.toString(4);
    log.info("Request = " + request);
    queueUtils.sendToRMFieldQueue(request, "update");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_UPDATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }
  
  @And("RM sends a update SPG job request")
  public void rmSendsAUpdateCaseRequest() throws URISyntaxException {
    JSONObject json = new JSONObject(getUpdateRMJson());
    json.remove("caseId");
    json.put("caseId", caseId);

    json.remove("caseRef");
    json.put("caseRef", caseRef);

    String request = json.toString(4);
    log.info("Request = " + request);
    queueUtils.sendToRMFieldQueue(request, "update");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_UPDATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  } 
 
  @When("the Gateway sends an Update Job message to TM")
  public void the_Gateway_sends_anUpdate_Job_message_to_TM() {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_UPDATE_PRE_SENDING, 10000L);
    assertThat(hasBeenTriggered).isTrue();
    List<GatewayEventDTO> events = gatewayEventMonitor.getEventsForEventType(COMET_UPDATE_PRE_SENDING, 1);
    event_COMET_UPDATE_ACK = events.get(0);  
  }

  @Then("a updated request is created of the right type")
  public void a_updated_request_is_created_of_the_right_type() {
 
    String actualCaseRef = event_COMET_UPDATE_ACK.getMetadata().get("Case Ref");
    String expectedCaseRef = caseRef;
    //if (isSecure.equals("T"))
    //  expectedCaseRef = "SECSS_" + caseRef;
    
    assertEquals("Case Ref created for TM", expectedCaseRef, actualCaseRef);
}
  
  @Then("the update job is acknowledged by tm")
  public void theUpdateJobIsAcknowledgedByTm() {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_UPDATE_ACK, 10000L);
   assertThat(hasBeenTriggered).isTrue();
  }

  @Then("the update job should fail")
  public void the_update_job_should_fail_by_tm() {
  boolean hasBeenTriggered = gatewayEventMonitor.hasErrorEventTriggered(caseId, ROUTING_FAILED, 10000L);
  assertThat(hasBeenTriggered).isTrue();
}

@Then("Gateway will reroute it as a create message")
public void gateway_will_reroute_it_as_a_create_message() {
  boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CONVERT_SPG_UNIT_UPDATE_TO_CREATE, 10000L);
  assertThat(hasBeenTriggered).isTrue();
}

@Then("Gateway will send a create job to TM")
public void gateway_will_send_a_create_job_to_TM() {
  boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_PRE_SENDING, 10000L);
  assertThat(hasBeenTriggered).isTrue();
}

@Then("the create job is acknowledged by tm")
public void the_create_job_is_acknowledged_by_tm() {
  boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
  assertThat(hasBeenTriggered).isTrue();
}

  private String getUpdateRMJson() {
    switch (type) {
    case "Estab" :
      return ceSpgEstabUpdateJson;
    case "Unit" :
      return ceSpgUnitUpdateJson;
    default:
      throw new RuntimeException("Incorrect survey " + survey + " and type " + type);
    }
  }

  private String getCreateRMJson() {
    switch (type) {
    case "Estab" :
      return ceSpgEstabCreateJson;
    case "Unit" :
      return ceSpgUnitCreateJson;
    default:
      throw new RuntimeException("Incorrect survey " + survey + " and type " + type);
    }
  }
}
