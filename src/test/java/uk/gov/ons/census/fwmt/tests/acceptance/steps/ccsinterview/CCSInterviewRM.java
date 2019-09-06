package uk.gov.ons.census.fwmt.tests.acceptance.steps.ccsinterview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.fasterxml.jackson.databind.JsonNode;
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
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

@Slf4j
@PropertySource("classpath:application.properties")
public class CCSInterviewRM {

  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  private static final String COMET_CREATE_JOB_REQUEST = "COMET_CREATE_SENT";
  private String receivedRMMessage = null;
  private boolean qIdHasValue;
  private String tmRequest = null;
  private JsonNode tmRequestRootNode;
  private String resourcePath;
  private ObjectMapper jsonObjectMapper = new ObjectMapper();
  private String caseId;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueClient;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    receivedRMMessage = Resources.toString(Resources.getResource("files/input/actionInstructionCCSIV.xml"), Charsets.UTF_8);

    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueClient.clearQueues();


    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
  }

  @Given("TM doesn't have an existing job CCS Interview with case ID {string}")
  public void aTMDoesntHaveAnExistingJobWithCaseId(String caseId) {
    try {
      tmMockUtils.getCaseById(caseId);
      fail("Case should not exist");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @And("RM sends a create CCS Interview job request")
  public void rmSendsACreateHouseHoldJobRequest() throws URISyntaxException, InterruptedException {
    String caseId = "e6e3e714-2f26-4909-a564-b8d4d0c8ba49";
    queueClient.sendToRMFieldQueue(receivedRMMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create CCS Interview Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    String caseId = "e6e3e714-2f26-4909-a564-b8d4d0c8ba49";
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_JOB_REQUEST, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new CCS Interview case with id of {string} is created in TM")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
  }

  @And("TM sends a CCS PL Outcome to the gateway with case ID {string}")
  public void tmSendsACCSPLOutcomeToTheGatewayWithCaseID(String caseId) {
    String fileLocation = "";
    this.qIdHasValue = false;
    resourcePath = "ccspl/household/";
    readRequest("Complete on paper (full)");
  }

  @And("the Outcome Service processes the CCS PL message and sends to RM")
  public void theOutcomeServiceProcessTheCCSPLMessage() {
    JsonNode node = tmRequestRootNode.path("caseId");
    caseId = node.asText();
    int response = tmMockUtils.sendTMCCSPLResponseMessage(tmRequest, caseId);
    assertEquals(202, response);
  }


  private void readRequest(String inputMessage) {
    this.tmRequest = getTmRequest(inputMessage);
    try {
      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing file", e);
    }
  }

  private String getTmRequest(String inputMessage) {
    try {
      String pathname = createPathnameFromOutcomeName(inputMessage);
      String message = Resources.toString(
              Resources.getResource("files/outcome/" + resourcePath + "/" + pathname + "/tmrequest" + (qIdHasValue?"-q":"") + ".json"), Charsets.UTF_8);
      return message;
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }
  private String createPathnameFromOutcomeName(String outcomeName) {
    String pathname = outcomeName.replaceAll("[^A-Za-z]+", "").toLowerCase();
    return pathname;
  }
}
