package uk.gov.ons.census.fwmt.tests.acceptance.steps;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class OutcomeCCSHouseHoldPLSteps {

  @Autowired
  private TMMockUtils tmMockUtils;
  
  @Autowired
  private QueueUtils queueUtils;

  private GatewayEventMonitor gatewayEventMonitor = new GatewayEventMonitor();

  private String tmRequest = null;

  private ObjectMapper jsonObjectMapper = new ObjectMapper();

  private JsonNode tmRequestRootNode;

  private String caseId;
  
  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  private String secondaryOutcome;
  
  public static final String OUTCOME_SENT_RM = "Outcome - Case Outcome Sent";

  private String actualMessage;

  private boolean qIdHasValue;
  
  private String resourcePath;

  private List<JsonNode> multipleMessages;
  
  @Before
  public void before() {
    try {
      gatewayEventMonitor.enableEventMonitor(rabbitLocation);
    } catch (IOException | TimeoutException e) {
      throw new RuntimeException("Problem with setting up", e);
    }
  }

  @After
  public void after() throws IOException, TimeoutException, URISyntaxException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();

    queueUtils.clearQueues();
  }

//  @Then("and of the correct {string}")
//  public void and_of_the_correct(String eventType) {
//    try {
//      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);
//      JsonNode node = actualMessageRootNode.path("event").path("type");
//      assertEquals(eventType, node.asText());
//    } catch (IOException e) {
//      throw new RuntimeException("Problem parsing ", e);
//    }
//  }

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
//
//  @Given("TM sends a Contact Made Census Case Outcome to the Gateway")
//  public void tm_sends_a_Contact_Made_Census_Case_Outcome_to_the_Gateway() {
//    try {
//      tmRequest = Resources.toString(
//          Resources.getResource("files/outcome/contactmade/fullfilmentrequests/tmrequest-multiple.json"), Charsets.UTF_8);
//      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
//    } catch (IOException e) {
//      throw new RuntimeException("Problem retrieving resource file", e);
//    }
//  }
//
//
//  @Then("the messages should be correct")
//  public void the_messages_should_be_correct() throws IOException {
//    try {
//      String message1 = Resources.toString(
//        Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple1.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message1)));
//
//      String message2 = Resources.toString(
//          Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple2.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message2)));
//
//      String message3 = Resources.toString(
//        Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple3.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message3)));
//   } catch (IOException e) {
//        throw new RuntimeException("Problem parsing file", e);
//      }
//  }
//
//  @Given("the message contains {string} fulfilment requests")
//  public void the_message_contains_fulfilment_requests(String quantity) {
//    int qty = Integer.parseInt(quantity);
//    int actualSize = tmRequestRootNode.path("fulfillmentRequests").size();
//    assertEquals(qty, actualSize);
//   }
//
//  @Then("the Outcome Service should create {string} messages")
//  public void the_Outcome_Service_should_create_messages(String quantity) throws InterruptedException, IOException {
//    multipleMessages = new ArrayList<>();
//    for(int i =0; i<3; i++) {
//      String messsage = queueUtils.getMessageOffQueueWithRoutingKey("Gateway.Fulfillment.Request", "event.fulfillment.request");
//      multipleMessages.add(jsonObjectMapper.readTree(messsage));
//    }
//  }
//
//  @Given("TM sends a Questionnaire Linked Contact Made Census Case Outcome to the Gateway")
//  public void tm_sends_a_Questionnaire_Linked_Contact_Made_Census_Case_Outcome_to_the_Gateway() {
//    try {
//      tmRequest = Resources.toString(
//          Resources.getResource("files/outcome/contactmade/fullfilmentrequests/tmrequest-multiple-q.json"), Charsets.UTF_8);
//      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
//    } catch (IOException e) {
//      throw new RuntimeException("Problem retrieving resource file", e);
//    }
//  }
//
//  @Then("the Questionnaire Linked messages should be correct")
//  public void the_Questionnaire_Linked_messages_should_be_correct() {
//    try {
//      String message1 = Resources.toString(
//        Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple-q1.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message1)));
//
//      String message2 = Resources.toString(
//          Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple-q2.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message2)));
//
//      String message3 = Resources.toString(
//        Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple-q3.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message3)));
//   } catch (IOException e) {
//        throw new RuntimeException("Problem parsing file", e);
//      }
//  }
//
//  @Given("TM sends a Mixed Contact Made Census Case Outcome to the Gateway")
//  public void tm_sends_a_Mixed_Contact_Made_Census_Case_Outcome_to_the_Gateway() {
//    try {
//      tmRequest = Resources.toString(
//          Resources.getResource("files/outcome/contactmade/fullfilmentrequests/tmrequest-multiple-mixed.json"), Charsets.UTF_8);
//      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
//    } catch (IOException e) {
//      throw new RuntimeException("Problem retrieving resource file", e);
//    }
//  }
//
//  @Then("the Mixed messages should be correct")
//  public void the_Mixed_messages_should_be_correct() {
//    try {
//      String message1 = Resources.toString(
//        Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple-mixed1.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message1)));
//
//      String message2 = Resources.toString(
//          Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple-mixed2.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message2)));
//
//      String message3 = Resources.toString(
//        Resources.getResource("files/outcome/contactmade/fullfilmentrequests/eventresponse-multiple-mixed3.json"), Charsets.UTF_8);
//      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message3)));
//   } catch (IOException e) {
//        throw new RuntimeException("Problem parsing file", e);
//      }
//  }


  @Given("TM sends a {string} Census Case CCS PL Outcome to the Gateway")
  public void tmSendsACensusCaseCCSPLOutcomeToTheGateway(String inputMessage) {
    this.qIdHasValue = false;
    resourcePath = "ccspl/household/";
    readRequest(inputMessage);
  }

  @And("the Primary Outcome for CCS PL is {string}")
  public void thePrimaryOutcomeForCCSPLIs(String primaryOutcome) {
    JsonNode node = tmRequestRootNode.path("primaryOutcome");
    assertEquals(primaryOutcome, node.asText());
  }

  @And("the Secondary Outcome for CCS PL is {string}")
  public void theSecondaryOutcomeForCCSPLIs(String secondaryOutcome) {
    JsonNode node = tmRequestRootNode.path("secondaryOutcome");
    assertEquals(secondaryOutcome, node.asText());
  }

  @When("the Outcome Service processes the CCS PL message")
  public void theOutcomeServiceProcessTheCCSPLMessage() {
    JsonNode node = tmRequestRootNode.path("caseId");
    caseId = node.asText();
    int response = tmMockUtils.sendTMCCSPLResponseMessage(tmRequest, caseId);
    assertEquals(202, response);
  }

  @Then("the Outcome Service for the CCS PL should create a valid {string} for the correct {string} and the outcome is of {string}")
  public void theOutcomeServiceForTheCCSPLShouldCreateAValidForTheCorrect(String caseEvent, String routingKey, String outcome) {
    gatewayEventMonitor.checkForEvent(caseId, OUTCOME_SENT_RM);
    try {
      actualMessage = queueUtils.getMessageOffQueueWithRoutingKey(caseEvent, routingKey);
      String jsonAsString = "";
      boolean isPresent;
      jsonAsString = jsonObjectMapper.writeValueAsString(actualMessage);
      isPresent = jsonAsString.contains(outcome);
      assertTrue(isPresent);
      //assertTrue(compareCaseEventMessages(secondaryOutcome, actualMessage));
    } catch (InterruptedException | JsonProcessingException e) {
      throw new RuntimeException("Problem getting message", e);
    }
  }

  @Then("and of the correct CCS {string}")
  public void and_of_the_correct(String eventType) {
    try {
      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);
      JsonNode node = actualMessageRootNode.path("event").path("type");
      assertEquals(eventType, node.asText());
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing ", e);
    }
  }

}
