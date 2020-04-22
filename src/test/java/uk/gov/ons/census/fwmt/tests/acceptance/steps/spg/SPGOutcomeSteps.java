package uk.gov.ons.census.fwmt.tests.acceptance.steps.spg;

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
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.SpgReasonCodeLookup;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Slf4j
public class SPGOutcomeSteps {

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueClient;

  @Autowired
  private SpgReasonCodeLookup spgReasonCodeLookup;

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

  public static final String CCSI_OUTCOME_SENT = "CCSI_OUTCOME_SENT";

  public static final String FIELD_REFUSALS_QUEUE = "Field.refusals";
  // TODO : add correct queses throught test suite
  public static final String TEMP_FIELD_OTHERS_QUEUE = "Field.other";

  public static final String GATEWAY_RESPONDENT_REFUSAL_ROUTING_KEY = "event.respondent.refusal";

  public static final String GATEWAY_ADDRESS_UPDATE_ROUTING_KEY = "event.case.address.update";

  public static final String GATEWAY_FULFILMENT_REQUEST_ROUTING_KEY = "event.fulfilment.request";

  public static final String GATEWAY_QUESTIONNAIRE_UPDATE_ROUTING_KEY = "event.questionnaire.update";

  private List<String> actualMessages = new ArrayList<>();

  private boolean qIdHasValue;

  private String resourcePath;

  private String eventType;

  private String secondaryOutcome;

  private String primaryOutcome;

  private String outcomeCode;

  private Map<String, Object> inputRoot = new HashMap<>();

  private Map<String, Object> outputRoot = new HashMap<>();

  private final String surveyType = "spg";

  private JsonNode rmJsonNode;

  @Before
  public void before() throws URISyntaxException {
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

  @Given("the Field Officer sends a {string}")
  public void theFieldOfficerSendsA(String outcomeType) {
    this.qIdHasValue = false;
    this.eventType = outcomeType;
  }

  @Given("the Primary Outcome is {string}")
  public void the_Primary_Outcome_is(String primaryOutcome) {
    this.primaryOutcome = primaryOutcome;
    inputRoot.put("primaryOutcomeDescription", primaryOutcome);
  }

  @And("the secondary Outcome {string}")
  public void theSecondaryOutcome(String secondaryOutcome) {
    this.secondaryOutcome = secondaryOutcome;
    inputRoot.put("secondaryOutcomeDescription", secondaryOutcome);
  }

  @And("Outcome code is {string}")
  public void outcomeCodeIs(String outcomeCode) {
    this.outcomeCode = outcomeCode;
    inputRoot.put("outcomeCode", outcomeCode);
  }

  @When("Gateway receives the outcome")
  public void gatewayReceivesTheOutcome() throws JsonProcessingException {
    String TMRequest = createOutcomeMessage(eventType + "-in", inputRoot, surveyType);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tmJsonNode = mapper.readTree(TMRequest);
    caseId = tmJsonNode.path("caseId").asText();
    readRequest(TMRequest);
    int response = tmMockUtils.sendTMSPGResponseMessage(tmRequest, caseId);
    assertEquals(200, response);
  }

  @Then("It will send an {string} messages to RM")
  public void itWillSendAnMessagesToRM(String operationList) {
    String[] splitEventTypes = operationList.split(",");
    List<String> operationsList;
    operationsList = Arrays.asList(splitEventTypes);
    for (String operation : operationsList) {
      // TODO : this event will differ based on the operation being performed
      gatewayEventMonitor.checkForEvent(caseId, "CESPG_OUTCOME_SENT");
      try {
        actualMessages.add(queueClient.getMessage(operationToQueue(operation)));
        for(String message : actualMessages) {
          assertTrue(compareCaseEventMessages(eventType, message));
        }
      } catch (InterruptedException e) {
        throw new RuntimeException("Problem getting message", e);
      }
    }
  }

  @And("each message conforms to {string}")
  public void eachMessageConformsTo(String outputMessageList) {
    String[] splitEventTypes = outputMessageList.split(",");
    List<String> eventTypeList;
    eventTypeList = Arrays.asList(splitEventTypes);
    int index = 0;
    // TODO : creating RM message further up, may just need to do for all
    for (String event : eventTypeList) {
      try {
        if (actualMessages.get(index) == null) break;
        JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessages.get(index));
        JsonNode node = actualMessageRootNode.path("event").path("type");
        assertEquals(rmJsonNode.path("event").path("type").asText(), node.asText());
      } catch (IOException e) {
        throw new RuntimeException("Problem parsing ", e);
      }
      index++;
    }
  }

  private void readRequest(String inputMessage) {
    this.tmRequest = inputMessage;
    try {
      tmRequestRootNode = jsonObjectMapper.readTree(tmRequest);
    } catch (IOException e) {
      throw new RuntimeException("Problem parsing file", e);
    }
  }

  private String operationToQueue(String operation) {
    switch (operation) {
    case "HARD_REFUSAL_RECEIVED":
    case "EXTRAORDINARY_REFUSAL_RECEIVED":
      return FIELD_REFUSALS_QUEUE;
    case "ADDRESS_NOT_VALID":
    case "ADDRESS_TYPE_CHANGED_HH":
    case "ADDRESS_TYPE_CHANGED_CE_EST":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "FULFILMENT_REQUESTED":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "LINKED_QID":
      return TEMP_FIELD_OTHERS_QUEUE;
    default:
      throw new RuntimeException("Problem matching operation");
    }
  }

  private boolean compareCaseEventMessages(String eventType, String actualMessage) {
    try {
      // TODO : handle this if lookup is null - if we wont only use this for ADDRESS_NOT_VALID
      outputRoot.put("reason", spgReasonCodeLookup.getLookup(outcomeCode));
      String rmOutcome = createOutcomeMessage(eventType + "-out", outputRoot, surveyType);
      ObjectMapper mapper = new ObjectMapper();
      rmJsonNode = mapper.readTree(rmOutcome);
      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);

      boolean isEqual = rmJsonNode.equals(actualMessageRootNode);
      if (!isEqual) {
        log.info("expected and actual caseEvents are not the same: \n expected:\n {} \n\n actual: \n {}",
            rmJsonNode, actualMessage);
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
          Resources.getResource("files/outcome/" + resourcePath + "/" + pathname + "/eventresponse" +
              (qIdHasValue ? "-q" : "") + ".json"), Charsets.UTF_8);
      return message;
    } catch (IOException e) {
      throw new RuntimeException("Problem retrieving resource file", e);
    }
  }

  private String createPathnameFromOutcomeName(String outcomeName) {
    return outcomeName.replaceAll("[^A-Za-z]+", "").toLowerCase();
  }

  public String createOutcomeMessage(String eventType, Map<String, Object> root, String surveyType) {
     String outcomeMessage = "";

    try {
      Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
      configuration.setClassForTemplateLoading(SPGOutcomeSteps.class, "/files/outcome/");
      configuration.setDefaultEncoding("UTF-8");
      configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
      configuration.setLogTemplateExceptions(false);
      configuration.setWrapUncheckedExceptions(true);

      Template temp = configuration.getTemplate(surveyType + "/" + eventType + ".ftl");
      try (StringWriter out = new StringWriter(); StringWriter outcomeEventMessage = new StringWriter()) {

        temp.process(root, out);
        out.flush();

        outcomeEventMessage.flush();
        outcomeMessage = out.toString();

      } catch (TemplateException e) {
        log.error("Error: ", e);
      }
    } catch (IOException e) {
      log.error("Error: ", e);
    }
    return outcomeMessage;
  }
}
