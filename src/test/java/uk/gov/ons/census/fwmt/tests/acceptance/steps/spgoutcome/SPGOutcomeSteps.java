package uk.gov.ons.census.fwmt.tests.acceptance.steps.spgoutcome;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.SpgReasonCodeLookup;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
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

  private static final String CESPG_OUTCOME_SENT = "CESPG_OUTCOME_SENT";

  private static final String RM_FIELD_QUEUE = "RM.Field";

  public static final String FIELD_REFUSALS_QUEUE = "Field.refusals";
  // TODO : add correct queses throught test suite
  public static final String TEMP_FIELD_OTHERS_QUEUE = "Field.other";

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

  private List<JsonNode> jsonNodeList = new ArrayList<>();

  private int index = 0;

  @Before
  public void before() {
    try {
      queueClient.createQueue();
      tmMockUtils.clearDownDatabase();
      gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
    } catch (Exception e) {
      throw new RuntimeException("Problem with setting up", e);
    }
  }

  @After
  public void after() throws URISyntaxException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    queueClient.clearQueues();
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

  @When("Gateway receives SPG outcome")
  public void gatewayReceivesSPGOutcome() throws JsonProcessingException {
    String TMRequest = createOutcomeMessage(eventType + "-in", inputRoot, surveyType);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode tmJsonNode = mapper.readTree(TMRequest);
    caseId = tmJsonNode.path("caseId").asText();
    readRequest(TMRequest);
    int response = tmMockUtils.sendTMSPGResponseMessage(tmRequest, caseId);
    assertEquals(200, response);
  }

  @When("Gateway receives SPG a New Unit Address outcome")
  public void gatewayReceivesSPGANewUnitAddressOutcome() {
    String TMRequest = createOutcomeMessage(eventType + "-in", inputRoot, surveyType);
    readRequest(TMRequest);
    int response = tmMockUtils.sendTMSPGNewUnitAddressResponseMessage(tmRequest);
    assertEquals(200, response);
  }

  @When("Gateway receives SPG New Standalone Address outcome")
  public void gatewayReceivesSPGNewStandaloneAddressOutcome() {
    String TMRequest = createOutcomeMessage(eventType + "-in", inputRoot, surveyType);
    readRequest(TMRequest);
    int response = tmMockUtils.sendTMSPGNewStandaloneAddressResponseMessage(tmRequest);
    assertEquals(200, response);
  }

  public void getCaseId() {
    Collection<GatewayEventDTO> message;
    message = gatewayEventMonitor.grabEventsTriggered(CESPG_OUTCOME_SENT, 1, 10000L);

    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered("N/A", CESPG_OUTCOME_SENT, 10000L);
    assertThat(hasBeenTriggered).isTrue();

    for (GatewayEventDTO retrieveCaseId : message) {
      caseId = retrieveCaseId.getCaseId();
    }
  }

  @Then("It will send an {string} messages to RM")
  public void itWillSendAnMessagesToRM(String operationList) {
    String[] splitEventTypes = operationList.split(",");
    List<String> operationsList;
    operationsList = Arrays.asList(splitEventTypes);
    int index = 0;
    for (String operation : operationsList) {
      gatewayEventMonitor.checkForEvent(caseId, CESPG_OUTCOME_SENT);
      try {
        actualMessages.add(queueClient.getMessage(operationToQueue(operation)));
      } catch (InterruptedException e) {
        throw new RuntimeException("Problem getting message", e);
      }
    }
    for(String message : actualMessages) {
      if (operationsList.get(index) == null) break;
      assertTrue(compareCaseEventMessages(operationsList.get(index), message));
      index++;
    }
  }

  @And("each message conforms to {string}")
  public void eachMessageConformsTo(String outputMessageList) {
    String[] splitEventTypes = outputMessageList.split(",");
    List<String> eventTypeList;
    eventTypeList = Arrays.asList(splitEventTypes);
    int index = 0;
    // TODO :
    for (String event : eventTypeList) {
      try {
        log.info("Processing event :" + event);
        if (actualMessages.get(index) == null) break;
        JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessages.get(index));
        JsonNode node = actualMessageRootNode.path("event").path("type");
        assertEquals(jsonNodeList.get(index).path("event").path("type").asText(), node.asText());
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
    case "REFUSAL_RECEIVED":
      return FIELD_REFUSALS_QUEUE;
    case "ADDRESS_NOT_VALID":
    case "ADDRESS_TYPE_CHANGED_HH":
    case "ADDRESS_TYPE_CHANGED_CE":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "FULFILMENT_REQUESTED":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "LINKED_QID":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "NEW_UNIT_ADDRESS":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "NEW_STANDALONE_ADDRESS":
      return TEMP_FIELD_OTHERS_QUEUE;
    case "CANCEL_FEEDBACK":
      return RM_FIELD_QUEUE;
    default:
      throw new RuntimeException("Problem matching operation");
    }
  }

  private boolean compareCaseEventMessages(String eventType, String actualMessage) {
    try {
      // TODO : we use for REFUSAL_RECEIVED and reason for ADDRESS_NOT_VALID - templates prevent null pointer
      outputRoot.put("reason", spgReasonCodeLookup.getLookup(outcomeCode));
      String rmOutcome = createOutcomeMessage(eventType + "-out", outputRoot, surveyType);
      ObjectMapper mapper = new ObjectMapper();
      JsonNode rmJsonNode = mapper.readTree(rmOutcome);
      jsonNodeList.add(rmJsonNode);
      JsonNode actualMessageRootNode = jsonObjectMapper.readTree(actualMessage);

      boolean isEqual = jsonNodeList.get(index).equals(actualMessageRootNode);
      if (!isEqual) {
        log.info("expected and actual caseEvents are not the same: \n expected:\n {} \n\n actual: \n {}",
            jsonNodeList.get(index), actualMessage);
      }
      this.index++;
      return isEqual;

    } catch (IOException e) {
      throw new RuntimeException("Problem comparing 2 json files", e);
    }
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
