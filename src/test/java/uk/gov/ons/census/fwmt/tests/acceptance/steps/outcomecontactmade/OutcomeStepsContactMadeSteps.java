package uk.gov.ons.census.fwmt.tests.acceptance.steps.outcomecontactmade;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

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
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

@Slf4j
@PropertySource("classpath:application.properties")
public class OutcomeStepsContactMadeSteps {

  private String testOutcomeJson = null;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueUtils queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private ObjectMapper objectMapper = new ObjectMapper();

  private String tmRequest = null;

  private ObjectMapper jsonObjectMapper = new ObjectMapper();

  private JsonNode tmRequestRootNode;

  private String caseId;

  private String secondaryOutcome;

  public static final String HH_OUTCOME_SENT = "HH_OUTCOME_SENT";

  private String actualMessage;

  private boolean qIdHasValue;

  private String resourcePath;

  private List<JsonNode> multipleMessages;

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    testOutcomeJson = null;
    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
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

    @Given("TM sends a {string} Census Case Outcome to the Gateway where {string}")
  public void tm_sends_a_Census_Case_Outcome_to_the_Gateway_where(String inputMessage, String qIdHasValue) {
    this.qIdHasValue = Boolean.valueOf(qIdHasValue);
    resourcePath = "household/contactmade";

    readRequest(inputMessage);
  }

  @And("the Primary Outcome is of {string}")
  public void the_Primary_Outcome_is(String primaryOutcome) {
    JsonNode node = tmRequestRootNode.path("primaryOutcome");
    assertEquals(primaryOutcome, node.asText());
  }

  @And("the Secondary Outcome is of {string}")
  public void the_Secondary_Outcome_is(String secondaryOutcome) {
    JsonNode node = tmRequestRootNode.path("secondaryOutcome");
    assertEquals(secondaryOutcome, node.asText());
  }

  @Then("of the correct {string}")
  public void and_of_the_correct(String eventType) {
    try {
      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);
      JsonNode node = actualMessageRootNode.path("event").path("type");
      assertEquals(eventType, node.asText());
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing ", e);
    }
  }

  @Given("TM sends a Contact Made Census Case Outcome to the Gateway")
  public void tm_sends_a_Contact_Made_Census_Case_Outcome_to_the_Gateway() {
    try {
      tmRequest = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/tmrequest-multiple.json"), Charsets.UTF_8);
      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }

  @Given("TM sends a Questionnaire Linked Contact Made Census Case Outcome to the Gateway")
  public void tm_sends_a_Questionnaire_Linked_Contact_Made_Census_Case_Outcome_to_the_Gateway() {
    try {
      tmRequest = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/tmrequest-multiple-q.json"), Charsets.UTF_8);
      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }

  @Given("TM sends a Mixed Contact Made Census Case Outcome to the Gateway")
  public void tm_sends_a_Mixed_Contact_Made_Census_Case_Outcome_to_the_Gateway() {
    try {
      tmRequest = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/tmrequest-multiple-mixed.json"), Charsets.UTF_8);
      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }

  @And("the message contains {string} fulfilment requests")
  public void the_message_contains_fulfilment_requests(String quantity) {
    int qty = Integer.parseInt(quantity);
    int actualSize = tmRequestRootNode.path("fulfilmentRequests").size();
    assertEquals(qty, actualSize);
  }

  @When("the Outcome Service process the message")
  public void the_Outcome_Service_process_the_message() {
    JsonNode node = tmRequestRootNode.path("caseId");
    caseId = node.asText();
    int response = tmMockUtils.sendTMResponseMessage(tmRequest, caseId);
    assertEquals(202, response);
  }

  @Then("a valid {string} for the correct {string}")
  public void the_Outcome_Service_should_create_a_valid_for_the_correct(String caseEvent, String routingKey) {
    gatewayEventMonitor.checkForEvent(caseId, HH_OUTCOME_SENT);
    try {
      actualMessage = queueUtils.getMessageOffQueueWithRoutingKey(caseEvent, routingKey);
      assertTrue(compareCaseEventMessages(secondaryOutcome, actualMessage));
    } catch (InterruptedException e) {
      throw new RuntimeException("Problem getting message", e);
    }
  }

  @And("the messages should be correct")
  public void the_messages_should_be_correct() throws IOException {
    try {
      String message1 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple1.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message1)));

      String message2 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple2.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message2)));

      String message3 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple3.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message3)));
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing file", e);
    }
  }

  @And("the Questionnaire Linked messages should be correct")
  public void the_Questionnaire_Linked_messages_should_be_correct() {
    try {
      String message1 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple-q1.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message1)));

      String message2 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple-q2.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message2)));

      String message3 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple-q3.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message3)));
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing file", e);
    }
  }

  @Then("the Mixed messages should be correct")
  public void the_Mixed_messages_should_be_correct() {
    try {
      String message1 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple-mixed1.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message1)));

      String message2 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple-mixed2.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message2)));

      String message3 = Resources.toString(
              Resources.getResource("files/outcome/household/contactmade/fulfilmentrequests/eventresponse-multiple-mixed3.json"), Charsets.UTF_8);
      assertTrue(multipleMessages.contains(jsonObjectMapper.readTree(message3)));
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing file", e);
    }
  }

  @Then("the service should create {string} messages")
  public void the_Outcome_Service_should_create_messages(String quantity) throws InterruptedException, IOException {
    multipleMessages = new ArrayList<>();
    for(int i =0; i<3; i++) {
      String messsage = queueUtils.getMessageOffQueueWithRoutingKey("Field.other", "event.fulfilment.request");
      multipleMessages.add(jsonObjectMapper.readTree(messsage));
    }
  }

}
