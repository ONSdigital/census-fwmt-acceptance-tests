package uk.gov.ons.census.fwmt.tests.acceptance.steps.spgcreate;

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
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Slf4j
public class SPGCreateSteps {

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";

  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

  private static final String COMET_UPDATE_ACK = "COMET_UPDATE_ACK";

  private static final String COMET_CANCEL_ACK = "COMET_CANCEL_ACK";

  private String request = null;

  private String ceSpgEstab = null;

  private String ceSpgUnit = null;

  private String spgUpdate = null;

  private String spgCancel = null;

  @Before
  public void setup() throws Exception {
    ceSpgEstab = Resources.toString(Resources.getResource("files/input/spg/spgEstabCreate.json"), Charsets.UTF_8);
    ceSpgUnit = Resources.toString(Resources.getResource("files/input/spg/spgUnitCreate.json"), Charsets.UTF_8);
    spgUpdate = Resources.toString(Resources.getResource("files/input/spg/spgUpdate.json"), Charsets.UTF_8);
    spgCancel = Resources.toString(Resources.getResource("files/input/spg/spgCancel.json"), Charsets.UTF_8);

    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    tmMockUtils.clearDownDatabase();

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
  }

  @After
  public void tearDownGatewayEventMonitor() throws Exception {
    try{
      gatewayEventMonitor.tearDownGatewayEventMonitor();
    }catch(Exception e){
      System.out.println(e);
    }
    tmMockUtils.disableRequestRecorder();

    queueUtils.clearQueues("RM.Field", "RM.FieldDLQ", "Outcome.Preprocessing", "Outcome.PreprocessingDLQ");
  }

  @Given("a TM doesnt have a {string} {string} job with case ID {string} in TM")
  public void aTMDoesntHaveAJobWithCaseIDInTM(String survey, String type, String caseId) {
    try {
      log.info("Looking for "+ caseId + " " + survey + " " + type + "within TM");
      tmMockUtils.getCaseById(caseId);
      fail("Case should not exist");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
    request = getInput(survey, type);
  }

  private String getInput(String survey, String type) {
    switch (type) {
    case "Estab" :
      return ceSpgEstab;
    case "Unit" :
      return ceSpgUnit;
    default:
      throw new RuntimeException("Incorrect survey " + survey + " and type " + type);
    }
  }

  @And("RM sends a create HouseHold job request")
  public void rmSendsACreateHouseHoldJobRequest() throws URISyntaxException {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    queueUtils.sendToRMFieldQueue(request, "create");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with id of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
  }

  @And("RM sends a update case request")
  public void rmSendsAUpdateCaseRequest() throws URISyntaxException {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    queueUtils.sendToRMFieldQueue(spgUpdate, "update");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @And("RM sends a cancel case request")
  public void rmSendsACancelCaseRequest() throws URISyntaxException {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    queueUtils.sendToRMFieldQueue(spgCancel, "cancel");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("the update job is acknowledged by tm")
  public void theUpdateJobIsAcknowledgedByTm() {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_UPDATE_ACK, 10000L);
   assertThat(hasBeenTriggered).isTrue();
  }

  @Then("the cancel job is acknowledged by tm")
  public void theCancelJobIsAcknowledgedByTm() {
    String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CANCEL_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }
}
