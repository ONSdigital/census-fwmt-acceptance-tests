package uk.gov.ons.census.fwmt.tests.acceptance.steps.cryptography;

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
import freemarker.template.TemplateExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common.CommonUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common.CommonUtils.testBucket;

@Slf4j
public class CryptographySteps {

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private GatewayEventMonitor gatewayEventMonitor;

  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";

  private static final String COMET_HH_OUTCOME_RECEIVED = "COMET_HH_OUTCOME_RECEIVED";

  private static final String DECRYPTED_HH_NAMES = "DECRYPTED_HH_NAMES";

  private static final String FIELD_REFUSALS_QUEUE = "Field.refusals";

  private final static String OUTCOME_SENT = "OUTCOME_SENT";

  private static final String COMET_CREATE_PRE_SENDING = "COMET_CREATE_PRE_SENDING";

  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

  private String hhHardRefusalCreate = null;

  private List<String> expectedProcessors = new ArrayList<>();

  private List<String> expectedRmMessages = new ArrayList<>();

  private List<String> expectedJsMessages = new ArrayList<>();

  private Map<String, String> actualRmMessageMap = new HashMap<>();

  private Map<String, String> expectedRmMessageMap = new HashMap<>();

  private String addressTypeChangeMsg;

  private String newCaseId;

  private Collection<GatewayEventDTO> rmOutcomeEvents;

  private Collection<GatewayEventDTO> jsOutcomeEvents;

  private final static String messageCaseId = "bd6345af-d706-43d3-a13b-8c549e081a76";

  private GatewayEventDTO event_COMET_CREATE_PRE_SENDING;

  @Autowired
  private QueueClient queueClient;

  @Autowired
  private CommonUtils commonUtils;

  private final ObjectMapper jsonObjectMapper = new ObjectMapper();

  @Before
  public void setup() throws Exception {
    hhHardRefusalCreate = Resources.toString(Resources.getResource("files/input/hh/hhHardRefusalCreate.json"), Charsets.UTF_8);
    commonUtils.setup();

    expectedProcessors.clear();
    expectedRmMessages.clear();
    expectedJsMessages.clear();
    actualRmMessageMap.clear();
    expectedRmMessageMap.clear();
    addressTypeChangeMsg = null;
    newCaseId = null;
  }

  @After
  public void clearDown() throws Exception {
    commonUtils.clearDown();
  }

  @Given("TM doesnt have a job with case ID {string} in TM")
  public void aTMDoesntHaveAJobWithCaseIDInTM(String caseId) {
    try {
      testBucket.put("caseId", caseId);
      tmMockUtils.getCaseById(caseId);
      fail("Case should not exist");
    } catch (HttpClientErrorException e) {
      assertEquals(HttpStatus.NOT_FOUND, e.getStatusCode());
    }
  }

  @When("the gateway receives a hard refusal for a household from TM with caseId {string}")
  public void theGatewayReceivesAHHHardRefusalFromTm(String caseId) throws Exception {
    String request = createOutcomeMessage("HARD_REFUSAL");
    int response = tmMockUtils.sendTMHHResponseMessage(request, caseId);
    assertEquals(200, response);

    boolean isMsgRecieved = gatewayEventMonitor.hasEventTriggered(caseId, COMET_HH_OUTCOME_RECEIVED, CommonUtils.TIMEOUT);
    assertThat(isMsgRecieved).isTrue();
  }

  @Then("the gateway will encrypt the name of the householder")
  public void gatewaywillEncryptHouseholderName() throws InterruptedException, JsonProcessingException {
    boolean isForenameBase64;
    boolean isSurnameBase64;
    String msg = queueClient.getMessage(FIELD_REFUSALS_QUEUE);
    String base64Regex = "([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)";
    Pattern patron = Pattern.compile(base64Regex);

    JsonNode actualMessageRootNode = jsonObjectMapper.readTree(msg);
    JsonNode contact = actualMessageRootNode.path("payload").path("refusal").path("contact");
    JsonNode foreName = contact.path("forename");
    JsonNode surname = contact.path("surname");

    isForenameBase64 = patron.matcher(foreName.asText()).matches();
    isSurnameBase64 = patron.matcher(surname.asText()).matches();

    assertTrue(isForenameBase64);
    assertTrue(isSurnameBase64);
  }

  @Then("the gateway will send the case to RM")
  public void theGatewayWillSendTheCaseToRM() throws JsonProcessingException {
    rmOutcomeEvents = gatewayEventMonitor.grabEventsTriggered(OUTCOME_SENT, expectedRmMessages.size(), CommonUtils.TIMEOUT);

    assertNotNull(rmOutcomeEvents);

    rmOutcomeEvents.clear();
  }

  @When("gateway receives a NC create from RM with case ID {string}")
  public void theGatewayReceivesAHHHardRefusalFromRm(String caseId) throws Exception {
    tmMockUtils.addNcHouseholderDetails(caseId);
    JSONObject json = new JSONObject(hhHardRefusalCreate);

    String request = json.toString(4);
    log.info("Request = " + request);
    queueClient.sendToRMFieldQueue(request, "create");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, CommonUtils.TIMEOUT);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("the gateway will retrieve and decrypt the householders name from the RM Case API for case ID {string}")
  public void gatewaywillRetrieveHouseholderName(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, DECRYPTED_HH_NAMES, CommonUtils.TIMEOUT);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("the gateway will send the case to TM")
  public void theGatewayWillSendTheCaseToTM() {
    Collection<GatewayEventDTO> message;
    String caseId = null;
    message = gatewayEventMonitor.grabEventsTriggered(COMET_CREATE_PRE_SENDING, 1, 10000L);

    for (GatewayEventDTO retrieveCaseId : message) {
      caseId = retrieveCaseId.getCaseId();
    }

    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, CommonUtils.TIMEOUT);
    assertThat(hasBeenTriggered).isTrue();
  }

  private String createOutcomeMessage(String eventType)
      throws Exception {
    String outcomeMessage = "";

    Configuration configuration = new Configuration(Configuration.VERSION_2_3_28);
    configuration.setClassForTemplateLoading(CryptographySteps.class, "/files/outcome/tm/");
    configuration.setDefaultEncoding("UTF-8");
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
    configuration.setLogTemplateExceptions(false);
    configuration.setWrapUncheckedExceptions(true);

    Template temp = configuration.getTemplate(eventType + "-in.ftl");

    try (StringWriter out = new StringWriter(); StringWriter outcomeEventMessage = new StringWriter()) {
      temp.process(null, out);
      out.flush();
      outcomeEventMessage.flush();
      outcomeMessage = out.toString();

    } finally {
    }
    return outcomeMessage;
  }


}
