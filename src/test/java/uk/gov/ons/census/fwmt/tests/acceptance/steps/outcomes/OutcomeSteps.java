package uk.gov.ons.census.fwmt.tests.acceptance.steps.outcomes;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import org.json.JSONObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.SpgReasonCodeLookup;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

@Slf4j
public class OutcomeSteps {

    private String surveyType;
    private String businessFunction;
    private String primaryOutcome;
    private String secondaryOutcome;
    private String outcomeCode;
    private boolean hasLinkedQid;
    private boolean hasFulfillmentRequest;
    private List<String> expectedProcessors;
    private List<String> expectedRmMessages;
    private List<String> expectedJsMessages;

    private final static String caseId = "bd6345af-d706-43d3-a13b-8c549e081a76";

    private final static String COMET_CESPG_OUTCOME_RECEIVED = "COMET_CESPG_OUTCOME_RECEIVED";

    private final static String COMET_CE_OUTCOME_RECEIVED = "COMET_CE_OUTCOME_RECEIVED";

    private final static String PROCESSING_OUTCOME = "PROCESSING_OUTCOME";

    private final static String OUTCOME_SENT = "OUTCOME_SENT";

    private final static String RM_FIELD_REPUBLISH = "RM_FIELD_REPUBLISH";

    private static final String FIELD_REFUSALS_QUEUE = "Field.refusals";

    private static final String TEMP_FIELD_OTHERS_QUEUE = "Field.other";

    private static final String RM_FIELD_QUEUE = "RM.Field";

    private static final String RM_FIELD_QUEUE_DLQ = "RM.FieldDLQ";

    private static final String OUTCOME_PRE_PROCESSING = "Outcome.Preprocessing";

    private static final String OUTCOME_PRE_PROCESSING_DLQ = "Outcome.PreprocessingDLQ";

    @Autowired
    private QueueClient queueClient;

    @Autowired
    private GatewayEventMonitor gatewayEventMonitor;

    @Autowired
    private TMMockUtils tmMockUtils;

    @Value("${service.rabbit.url}")
    private String rabbitLocation;

    @Value("${service.rabbit.username}")
    private String rabbitUsername;

    @Value("${service.rabbit.password}")
    private String rabbitPassword;

    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

    @Autowired
    private SpgReasonCodeLookup spgReasonCodeLookup;

    @Before
    public void setup() throws IOException, URISyntaxException, TimeoutException {
        gatewayEventMonitor.tearDownGatewayEventMonitor();
        queueClient.clearQueues(FIELD_REFUSALS_QUEUE, TEMP_FIELD_OTHERS_QUEUE, RM_FIELD_QUEUE, RM_FIELD_QUEUE_DLQ, OUTCOME_PRE_PROCESSING,
                OUTCOME_PRE_PROCESSING_DLQ);

        gatewayEventMonitor = new GatewayEventMonitor();
        gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
    }

    @After
    public void tearDownGatewayEventMonitor() throws IOException {
        gatewayEventMonitor.tearDownGatewayEventMonitor();
    }

    @Given("an {string} {string} outcome message")
    public void outcomeservice_receives_a_outcome_message(String surveyType, String businessFunction) {
        this.surveyType = surveyType;
        this.businessFunction = businessFunction;
    }

    @Given("its Primary Outcome is {string}")
    public void its_Primary_Outcome_is(String primaryOutcome) {
        this.primaryOutcome = primaryOutcome;
    }

    @Given("its secondary Outcome {string}")
    public void its_secondary_Outcome(String secondaryOutcome) {
        this.secondaryOutcome = secondaryOutcome;
    }

    @Given("its Outcome code is {string}")
    public void its_Outcome_code_is(String outcomeCode) {
        this.outcomeCode = outcomeCode;
    }

    @Given("the message includes a Linked QID {string}")
    public void the_message_includes_a_Linked_QID(String hasLinkedQid) {
        this.hasLinkedQid = "T".equals(hasLinkedQid);
    }

    @Given("the message includes a Fulfillment Request {string}")
    public void the_message_includes_a_Fulfillment_Request(String hasFulfillmentRequest) {
        this.hasFulfillmentRequest = "T".equals(hasFulfillmentRequest);
    }

    @When("Gateway receives the outcome")
    public void gateway_processes_the_outcome() throws Exception {
        sendTMOutcomeMessage();
        confirmOutcomeServiceReceivesMessage();
    }

    @Then("It will run the following processors {string}")
    public void it_will_run_the_following_processors(String processors) {
        String[] processorsArray = (!Strings.isBlank(processors)) ? processors.split(",") : new String[0];
        expectedProcessors = Arrays.asList(processorsArray);
        confirmProcessorsAreExcecuted();
    }

    @Then("create the following messages to RM {string}")
    public void create_the_following_messages_to_RM(String rmMessages) {
        String[] rmMessagesArray = (!Strings.isBlank(rmMessages)) ? rmMessages.split(",") : new String[0];
        expectedRmMessages = Arrays.asList(rmMessagesArray);
        confirmRmMessagesAreSent();
    }

    @Then("each message has the correct values")
    public void each_message_has_the_correct_values() throws Exception {
        confirmMessagesAreValid();
    }

    @Then("it will create the following messages {string} to JobService")
    public void it_will_create_the_following_messages_to_JobService(String jsMessages) {
        String[] jsMessagesArray = (!Strings.isBlank(jsMessages)) ? jsMessages.split(",") : new String[0];
        expectedJsMessages = Arrays.asList(jsMessagesArray);
        confirmJsMessagesAreSent();
    }

    private void confirmProcessorsAreExcecuted() {
        Collection<GatewayEventDTO> events = gatewayEventMonitor.grabEventsTriggered(PROCESSING_OUTCOME, expectedProcessors.size(), 5000L);
        List<String> actualProcessors = events.stream().filter(e -> e.getMetadata().get("survey type").equals(surveyType))
                .map(e -> e.getMetadata().get("processor")).collect(Collectors.toList());
        assertEquals(expectedProcessors.size(), actualProcessors.size());
        assertThat(expectedProcessors.containsAll(actualProcessors));
    }

    private void confirmRmMessagesAreSent() {
        Collection<GatewayEventDTO> events = gatewayEventMonitor.grabEventsTriggered(OUTCOME_SENT, expectedRmMessages.size(), 5000L);
        List<String> actualMessages = events.stream().filter(e -> e.getMetadata().get("survey type").equals(surveyType)).map(e -> e.getMetadata().get("type"))
                .collect(Collectors.toList());
        assertEquals(expectedRmMessages.size(), actualMessages.size());
        assertThat(expectedRmMessages.containsAll(actualMessages));
    }

    private void confirmJsMessagesAreSent() {
        Collection<GatewayEventDTO> events = gatewayEventMonitor.grabEventsTriggered(RM_FIELD_REPUBLISH, expectedJsMessages.size(), 5000L);
        List<String> actualMessages = events.stream().filter(e -> e.getMetadata().get("survey type").equals(surveyType))
                .map(e -> e.getMetadata().get("action instruction")).collect(Collectors.toList());
        assertEquals(expectedJsMessages.size(), actualMessages.size());
        assertThat(expectedJsMessages.containsAll(actualMessages));
    }

    private void sendTMOutcomeMessage() throws Exception {
        int response = -1;
        String request = getTmOutcomeRequest();
        switch (surveyType) {
        case "SPG":
            response = tmMockUtils.sendTMSPGResponseMessage(request, caseId);
            break;
        case "CE":
            response = tmMockUtils.sendTMCEResponseMessage(request, caseId);
            break;
        default:
            break;
        }
        assertEquals(200, response);
    }

    private String getTmOutcomeRequest() throws Exception {
        Map<String, Object> root = new HashMap();

        String linkedQid = (hasLinkedQid) ? createOutcomeMessage("LINKED_QID", root) : null;
        String fulfilmentRequested = (hasFulfillmentRequest) ? createOutcomeMessage("FULFILMENT_REQUESTED", root) : null;

        root.put("caseId", caseId);
        root.put("primaryOutcomeDescription", primaryOutcome);
        root.put("secondaryOutcomeDescription", secondaryOutcome);
        root.put("outcomeCode", outcomeCode);
        root.put("linkedQid", linkedQid);
        root.put("fulfilmentRequested", fulfilmentRequested);

        try {
            String request = null;
            switch (businessFunction) {
            case "Not Valid Address":
                request = createOutcomeMessage("ADDRESS_NOT_VALID", root);
                break;
            case "Hard Refusal":
            case "Extraordinary Refusal":
                request = createOutcomeMessage("REFUSAL_RECEIVED", root);
                break;
            case "Address Type Changed HH":
                request = createOutcomeMessage("ADDRESS_TYPE_CHANGED_HH", root);
                break;
            case "Address Type Changed CE":
                request = createOutcomeMessage("ADDRESS_TYPE_CHANGED_CE", root);
                break;
            case "Address Type Changed SPG":
                request = createOutcomeMessage("ADDRESS_TYPE_CHANGED_SPG", root);
                break;

            default:
                break;
            }
            System.out.print("REQUEST:  " + request);
            return request;
        } catch (Exception e) {
            throw new RuntimeException("Problem with setting up", e);
        }
    }

    private void confirmOutcomeServiceReceivesMessage() {
        String event = null;
        switch (surveyType) {
        case "SPG":
            event = COMET_CESPG_OUTCOME_RECEIVED;
            break;
        case "CE":
            event = COMET_CE_OUTCOME_RECEIVED;
            break;
        default:
            break;
        }
        boolean isMsgRecieved = gatewayEventMonitor.hasEventTriggered(caseId, event, 2000L);
        assertThat(isMsgRecieved).isTrue();
    }

    private String createOutcomeMessage(String eventType, Map<String, Object> root)
            throws Exception {
        String outcomeMessage = "";

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setClassForTemplateLoading(OutcomeSteps.class, "/files/outcome/tm/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);

        Template temp = configuration.getTemplate(eventType + "-in.ftl");
        try (StringWriter out = new StringWriter(); StringWriter outcomeEventMessage = new StringWriter()) {

            temp.process(root, out);
            out.flush();

            outcomeEventMessage.flush();
            outcomeMessage = out.toString();

        } finally {
        }
        return outcomeMessage;
    }

    private String operationToQueue(String operation) {
        switch (operation) {
        case "REFUSAL_RECEIVED":
            return FIELD_REFUSALS_QUEUE;
        case "ADDRESS_NOT_VALID":
        case "ADDRESS_TYPE_CHANGED":
        case "FULFILMENT_REQUESTED":
        case "QUESTIONNAIRE_LINKED":
        case "NEW_ADDRESS_REPORTED":
            return TEMP_FIELD_OTHERS_QUEUE;
        default:
            throw new RuntimeException("Problem matching operation");
        }
    }

    private String createExpectedRmMessage(String rmMessageType, Map<String, Object> root) throws Exception {
        String inputMessage = "";
        if ("ADDRESS_TYPE_CHANGED".equals(rmMessageType)) {
            switch (businessFunction) {
            case "Address Type Changed HH":
                rmMessageType = rmMessageType + "_HH";
                break;
            case "Address Type Changed CE":
                rmMessageType = rmMessageType + "_CE";
                break;
            case "Address Type Changed SPG":
                rmMessageType = rmMessageType + "_SPG";
                break;
            default:
                break;
            }
        }

        Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
        configuration.setClassForTemplateLoading(OutcomeSteps.class, "/files/outcome/rm/");
        configuration.setDefaultEncoding("UTF-8");
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);

        Template temp = configuration.getTemplate(rmMessageType + "-out.ftl");
        try (
                StringWriter out = new StringWriter();
                StringWriter inputEventMessage = new StringWriter()) {

            temp.process(root, out);
            out.flush();

            inputEventMessage.flush();
            inputMessage = out.toString();

        } finally {
        }
        return inputMessage;
    }

    private void confirmMessagesAreValid() throws Exception {
        Map<String, String> rmMessageMap = new HashMap<>();
        for (String rmMessageType : expectedRmMessages) {
            String msg = queueClient.getMessage(operationToQueue(rmMessageType));

            JsonNode actualMessageRootNode = jsonObjectMapper.readTree(msg);
            JsonNode typeNode = actualMessageRootNode.path("event").path("type");
            rmMessageMap.put(typeNode.asText(), msg);
        }
        assertEquals(expectedRmMessages.size(), rmMessageMap.size());
        assertThat(expectedRmMessages.containsAll(rmMessageMap.keySet()));

        Map<String, Object> root = new HashMap();
        for (String rmMessageType : expectedRmMessages) {
            UUID newCaseId = UUID.randomUUID();

            root.clear();
            root.put("reason", spgReasonCodeLookup.getLookup(outcomeCode));
            root.put("fulfilmentCode", outcomeCode);
            root.put("newCaseId", newCaseId.toString());
            String expectedRmMessage = createExpectedRmMessage(rmMessageType, root);
            expectedRmMessage = addNewCaseId(expectedRmMessage, newCaseId);

            JsonNode expectedJson = jsonObjectMapper.readTree(expectedRmMessage);
            String actualRmMessage = rmMessageMap.get(rmMessageType);
            actualRmMessage = addNewCaseId(actualRmMessage, newCaseId);
            JsonNode actualJson = jsonObjectMapper.readTree(actualRmMessage);
            boolean isEqual = expectedJson.equals(actualJson);
            if (!isEqual) {
                log.info("expected and actual caseEvents are not the same: \n expected:\n {} \n\n actual: \n {}",
                        expectedJson.toPrettyString(), actualJson.toPrettyString());
            }
            assertThat(isEqual).isTrue();
        }
    }

    private String addNewCaseId(String actualRmMessage, UUID newCaseId) {
        String modifiedJson = actualRmMessage;
        JSONObject jsonObject = new JSONObject(actualRmMessage);
        if (jsonObject.has("payload")) {
            JSONObject payload = jsonObject.getJSONObject("payload");
            if (payload.has("addressTypeChange")) {
                JSONObject addressTypeChange = payload.getJSONObject("addressTypeChange");

                if (addressTypeChange.has("newCaseId")) {
                    addressTypeChange.remove("newCaseId");
                    addressTypeChange.put("newCaseId", newCaseId.toString());
                    modifiedJson = jsonObject.toString();
                }
            }
        }
        return modifiedJson;
    }
}