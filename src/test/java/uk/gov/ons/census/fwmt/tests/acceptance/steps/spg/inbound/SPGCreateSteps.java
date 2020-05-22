package uk.gov.ons.census.fwmt.tests.acceptance.steps.spg.inbound;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.net.URISyntaxException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

@Slf4j
public class SPGCreateSteps {

  @Autowired
  private SPGCommonUtils spgCommonUtils;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueUtils;

  @Autowired
  private GatewayEventMonitor gatewayEventMonitor;

  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";

  private static final String COMET_CREATE_PRE_SENDING = "COMET_CREATE_PRE_SENDING";

  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

  private String ceSpgEstabCreateJson = null;

  private String ceSpgUnitCreateJson = null;

  private String survey = null;

  private String type = null;

  private String caseId = null;

  private GatewayEventDTO event_COMET_CREATE_PRE_SENDING;


  @Before
  public void setup() throws Exception {
    ceSpgEstabCreateJson = Resources.toString(Resources.getResource("files/input/spg/spgEstabCreate.json"), Charsets.UTF_8);
    ceSpgUnitCreateJson = Resources.toString(Resources.getResource("files/input/spg/spgUnitCreate.json"), Charsets.UTF_8);
    spgCommonUtils.setup();
  }

  @After
  public void clearDown() throws Exception {
    spgCommonUtils.clearDown();
  }

  @Given("a TM doesnt have a job with case ID {string} in TM")
  public void aTMDoesntHaveAJobWithCaseIDInTM(String caseId) {
    try {
      this.caseId = caseId;
      log.info("Looking for "+ caseId + " " + survey + " " + type + "within TM");
      tmMockUtils.getCaseById(caseId);
      fail("Case should not exist");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @And("RM sends a create SPG job request with {string} {string} {string} {string} {string}")
  public void rmSendsACreateHouseHoldJobRequest(String caseRef, String survey, String type, String isSecure, String isHandDeliver) throws URISyntaxException {
    this.survey = survey;
    this.type = type;

    JSONObject json = new JSONObject(getCreateRMJson());
    json.remove("caseId");
    json.put("caseId", caseId);

    json.remove("caseRef");
    json.put("caseRef", caseRef);

    if ("T".equals(isSecure)){
      json.remove("secureEstablishment");
      json.put("secureEstablishment", true);
    }

    if ("T".equals(isHandDeliver)){
      json.remove("handDeliver");
      json.put("handDeliver", true);
    }

    String request = json.toString(4);
    log.info("Resquest = " + request);
    queueUtils.sendToRMFieldQueue(request, "create");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_PRE_SENDING, 10000L);
    assertThat(hasBeenTriggered).isTrue();
    List<GatewayEventDTO> events = gatewayEventMonitor.getEventsForEventType(COMET_CREATE_PRE_SENDING, 1);
    event_COMET_CREATE_PRE_SENDING = events.get(0);
  }

  @Then("a new case is created of the right {string}")
  public void a_new_case_is_created_of_the_right_type(String expectedSurveyType) {
    String actualSurveyType = event_COMET_CREATE_PRE_SENDING.getMetadata().get("Survey Type");
    assertEquals("Survey Types created for TM", expectedSurveyType, actualSurveyType);
  }

  @And("the right caseRef {string}")
  public void and_the_right_caseref(String expectedCaseRef) {
    String actualCaseRef = event_COMET_CREATE_PRE_SENDING.getMetadata().get("Case Ref");
    assertEquals("Case Ref created for TM", expectedCaseRef, actualCaseRef);
  }

  @Then("a new case with id of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();

    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
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
