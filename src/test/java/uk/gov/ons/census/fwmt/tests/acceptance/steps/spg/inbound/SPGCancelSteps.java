package uk.gov.ons.census.fwmt.tests.acceptance.steps.spg.inbound;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URISyntaxException;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.springframework.beans.factory.annotation.Autowired;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;

@Slf4j
public class SPGCancelSteps {

  @Autowired
  private SPGCommonUtils spgCommonUtils;

  @Autowired
  private QueueClient queueUtils;

  @Autowired
  private GatewayEventMonitor gatewayEventMonitor;

  private static final String RM_CANCEL_REQUEST_RECEIVED = "RM_CANCEL_REQUEST_RECEIVED";

  private static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";
  private static final String ROUTING_FAILED = "ROUTING_FAILED";

  private String spgCancel = null;

  private String caseId = null;

  @Before
  public void setup() throws Exception {
    spgCommonUtils.setup();
    spgCancel = Resources.toString(Resources.getResource("files/input/spg/spgCancel.json"), Charsets.UTF_8);
  }

  @After
  public void clearDown() throws Exception {
    spgCommonUtils.clearDown();
  }

  @Given("RM sends a cancel case request")
  public void rm_sends_a_cancel_case_request() throws URISyntaxException {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    queueUtils.sendToRMFieldQueue(spgCancel, "cancel");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CANCEL_REQUEST_RECEIVED, 10000L);
    assertTrue(hasBeenTriggered);
  }

  @Then("the cancel job is acknowledged by tm")
  public void the_cancel_job_is_acknowledged_by_tm() {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CANCEL_ACK, 10000L);
    assertTrue(hasBeenTriggered);
  }

  @Then("the cancel job should fail")
  public void the_cancel_job_should_fail() {
    //TODO Move these caseIds
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    boolean hasBeenTriggered = gatewayEventMonitor.hasErrorEventTriggered(caseId, ROUTING_FAILED, 10000L);
    assertTrue(hasBeenTriggered);
  }
}
