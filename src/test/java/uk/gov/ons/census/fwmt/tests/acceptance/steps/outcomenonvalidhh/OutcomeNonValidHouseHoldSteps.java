package uk.gov.ons.census.fwmt.tests.acceptance.steps.outcomenonvalidhh;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class OutcomeNonValidHouseHoldSteps {

  @Autowired
  private TMMockUtils tmMockUtils;
  
  @Autowired
  private QueueClient queueClient;

  private GatewayEventMonitor gatewayEventMonitor = new GatewayEventMonitor();

  private String tmRequest = null;

  private ObjectMapper jsonObjectMapper = new ObjectMapper();

  private JsonNode tmRequestRootNode;

  private String caseId;
  
  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private String secondaryOutcome;
  
  public static final String HH_OUTCOME_SENT = "HH_OUTCOME_SENT";

  private String actualMessage;

  private boolean qIdHasValue;
  
  private String resourcePath;

  private List<JsonNode> multipleMessages;
  
  @Before
  public void before() {
    try {
      queueClient.createQueue();
      gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
    } catch (IOException | TimeoutException | InterruptedException e) {
      throw new RuntimeException("Problem with setting up", e);
    }

  }

  @After
  public void after() throws IOException, TimeoutException, URISyntaxException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
  }
  
  @Given("TM sends a {string} Census Case Outcome to the Gateway")
  public void tm_sends_a_Census_Case_Outcome_to_the_Gateway(String inputMessage) {
    this.qIdHasValue = false;  
    resourcePath = "household/nonvalidhousehold";
    readRequest(inputMessage);
  }

  @And("the Primary Outcome is {string}")
  public void the_Primary_Outcome_is(String primaryOutcome) {
    JsonNode node = tmRequestRootNode.path("primaryOutcome");
    assertEquals(primaryOutcome, node.asText());
  }

  @And("the Secondary Outcome is {string}")
  public void the_Secondary_Outcome_is(String secondaryOutcome) {
    JsonNode node = tmRequestRootNode.path("secondaryOutcome");
    assertEquals(secondaryOutcome, node.asText());
  }

  @When("the Outcome Service process non-valid household message")
  public void the_Outcome_Service_process_the_message() {
    JsonNode node = tmRequestRootNode.path("caseId");
    caseId = node.asText();
    int response = tmMockUtils.sendTMResponseMessage(tmRequest, caseId);
    assertEquals(202, response);
  }

  @Then("the Outcome Service should create a valid {string}")
  public void the_Outcome_Service_should_create_a_valid_for_the_correct(String caseEvent) {
    gatewayEventMonitor.checkForEvent(caseId, HH_OUTCOME_SENT);
    try {
      actualMessage = queueClient.getMessage(caseEvent);
      assertTrue(compareCaseEventMessages(secondaryOutcome, actualMessage));
    } catch (InterruptedException e) {
      throw new RuntimeException("Problem getting message", e);
    }
  }

  @Then("and of the correct {string}")
  public void and_of_the_correct(String eventType) {
    try {
      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);
      JsonNode node = actualMessageRootNode.path("event").path("type");
      assertEquals(eventType, node.asText());
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing ", e);
    }
  }

  private String createPathnameFromOutcomeName(String outcomeName) {
    String pathname = outcomeName.replaceAll("[^A-Za-z]+", "").toLowerCase();
    return pathname;
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

  private String getExpectedCaseEvent(String so) {
    try {
      String pathname = createPathnameFromOutcomeName(so);
      String message = Resources.toString(
          Resources.getResource("files/outcome/" + resourcePath + "/" + pathname + "/eventresponse" + (qIdHasValue?"-q":"") + ".json"), Charsets.UTF_8);
      return message;
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }

  private boolean compareCaseEventMessages(String so, String actualMessage) {
    try {
      String expectedCaseEvent = getExpectedCaseEvent(so);
      JsonNode expectedMessageRootNode = jsonObjectMapper.readTree(expectedCaseEvent);
      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);
            
      boolean isEqual = expectedMessageRootNode.equals(actualMessageRootNode);
      if (!isEqual) {
        log.info("expected and actual caseEvents are not the same: \n expected:\n {} \n\n actual: \n {}", expectedCaseEvent, actualMessage);
      }
      return isEqual;
      
    } catch (IOException e) {
      throw new RuntimeException("Problem comparing 2 json files", e);
    }
  }
  
  private void readRequest(String inputMessage) {
    this.tmRequest = getTmRequest(inputMessage);
    this.secondaryOutcome = inputMessage;
    try {
      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing file", e);
    }
  }

  @Then("the Outcome Service should create {string} messages")
  public void the_Outcome_Service_should_create_messages(String quantity) throws InterruptedException, IOException {
    multipleMessages = new ArrayList<>();
    for(int i =0; i<3; i++) {
      String message = queueClient
          .getMessage("Gateway.Fulfillment.Request");
      multipleMessages.add(jsonObjectMapper.readTree(message));
    }
  }


  
  
}
