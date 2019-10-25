package uk.gov.ons.census.fwmt.tests.acceptance.steps.ccsinterview;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.*;
import java.net.URISyntaxException;
import java.util.Collection;
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
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

@Slf4j
@PropertySource("classpath:application.properties")
public class CCSInterviewRM {

  public static final String CCSPL_OUTCOME_SENT = "CCSPL_OUTCOME_SENT";
  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  private String receivedRMMessage = null;
  private boolean qIdHasValue;
  private String tmRequest = null;
  private JsonNode tmRequestRootNode;
  private String resourcePath;
  private ObjectMapper jsonObjectMapper = new ObjectMapper();
  private String caseId;
  private String newCaseId;

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
    Collection<GatewayEventDTO> message;
    message = gatewayEventMonitor.grabEventsTriggered(CCSPL_OUTCOME_SENT, 1, 10000L);

    for (GatewayEventDTO retrieveCaseId : message) {
      newCaseId = retrieveCaseId.getCaseId();
    }
    String updatedRmMessage = updateActionInstructionWithNewCaseId();
    queueClient.sendToRMFieldQueue(updatedRmMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(newCaseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a Create CCS Interview Job message to TM")
  public void theGatewaySendsACreateJobMessageToTM() {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(newCaseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new CCS Interview case with a new case id is created in TM")
  public void aNewCaseIsCreatedInTm() throws InterruptedException {
    Thread.sleep(1000);
    ModelCase modelCase = tmMockUtils.getCaseById(newCaseId);
    assertEquals(newCaseId, modelCase.getId().toString());
  }

  @And("TM sends a CCS PL Outcome to the gateway with case ID {string}")
  public void tmSendsACCSPLOutcomeToTheGatewayWithCaseID(String caseId) {
    String fileLocation = "";
    this.qIdHasValue = false;
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
              Resources.getResource("files/input/ccsInterviewE2E.json"), Charsets.UTF_8);
      return message;
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }
  private String createPathnameFromOutcomeName(String outcomeName) {
    String pathname = outcomeName.replaceAll("[^A-Za-z]+", "").toLowerCase();
    return pathname;
  }

  private String updateActionInstructionWithNewCaseId () {
    try {
      File file = new File("src/test/resources/files/input/actionInstructionCCSIV.xml");

      DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
      Document document = documentBuilder.parse(file);

      Node updateCaseId = document.getElementsByTagName("caseId").item(0);
      updateCaseId.setTextContent(newCaseId);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      DOMSource domSource = new DOMSource(document);
      StreamResult streamResult = new StreamResult(new StringWriter());
      transformer.transform(domSource, streamResult);

      String updatedMessage = streamResult.getWriter().toString();

      return updatedMessage;
    } catch (Exception e){
      String error = "Errored";
    }
    return null;
  }

}
