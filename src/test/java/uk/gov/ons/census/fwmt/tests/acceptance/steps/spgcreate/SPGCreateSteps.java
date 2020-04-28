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

  private String receivedRMMessage = null;

  @Before
  public void setup() throws Exception {
    receivedRMMessage = Resources.toString(Resources.getResource("files/input/spg/spgEstabCreate.json"), Charsets.UTF_8);

    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    tmMockUtils.clearDownDatabase();

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
  }

  @After
  public void tearDownGatewayEventMonitor() throws Exception {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
    queueUtils.clearQueues();
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
    String caseId = "8dd42be3-09e6-488e-b4e2-0f14259acb9ef";
    queueUtils.sendToRMFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    String caseId = "8dd42be3-09e6-488e-b4e2-0f14259acb9e";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with id of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
  }
}
