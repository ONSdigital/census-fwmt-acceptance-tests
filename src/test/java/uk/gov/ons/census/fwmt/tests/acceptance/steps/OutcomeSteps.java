package uk.gov.ons.census.fwmt.tests.acceptance.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import uk.gov.ons.census.fwmt.common.data.comet.HouseholdOutcome;
import uk.gov.ons.census.fwmt.common.data.rm.OutcomeEvent;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class OutcomeSteps {

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

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    testOutcomeJson = null;
    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
  }

  @Given("TM sends a {string} Census Case Outcome to the Gateway with case ID {string}")
  public void tmSendsACensusCaseOutcomeToTheGatewayWithCaseID(String outcomeType, String caseId) throws IOException {
    setTestJson(outcomeType);

    int response = tmMockUtils.sendTMResponseMessage(testOutcomeJson, caseId);
    assertEquals(202, response);
  }

  private void setTestJson(String outcomeType) throws IOException {
    switch (outcomeType) {
    case "derelict":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/noValidHouseHoldDerelict.txt"), Charsets.UTF_8);
      break;
    case "splitAddress":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/contactMadeSplitAddress.txt"), Charsets.UTF_8);
      break;
    case "hardRefusal":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/contactMadeHardRefusal.txt"), Charsets.UTF_8);
      break;
    case "willComplete":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/fulfillment/contactMadeFulfillmentWillComplete.txt"),
              Charsets.UTF_8);
      break;
    case "haveCompleted":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/fulfillment/contactMadeFulfillmentHaveComplete.txt"),
              Charsets.UTF_8);
      break;
    case "collectedCompletedQuestionnaire":
      testOutcomeJson = Resources
          .toString(Resources.getResource(
              "files/outcome/fulfillment/contactMadeFulfillmentCollectedCompletedQuestionnaire.txt"), Charsets.UTF_8);
      break;
    case "callBackAnotherTime":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/fulfillment/contactMadeFulfillmentCallBackAnotherTime.txt"),
              Charsets.UTF_8);
      break;
    case "holidayHome":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/fulfillment/contactMadeFulfillmentHolidayHome.txt"),
              Charsets.UTF_8);
      break;
    case "secondResidence":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/fulfillment/contactMadeFulfillmentSecondResidence.txt"),
              Charsets.UTF_8);
      break;
    case "requestedAssistance":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/fulfillment/contactMadeFulfillmentRequestedAssistance.txt"),
              Charsets.UTF_8);
      break;
    case "householdPaperRequest":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/questionnaireRequests/householdPaperRequest.txt"),
              Charsets.UTF_8);
      break;
    case "householdContinuationRequest":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/questionnaireRequests/householdContinuationRequest.txt"),
              Charsets.UTF_8);
      break;
    case "householdIndividualRequest":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/questionnaireRequests/householdIndividualRequest.txt"),
              Charsets.UTF_8);
      break;
    case "multipleQuestionnaireRequest":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/questionnaireRequests/householdMultipleRequest.txt"),
              Charsets.UTF_8);
      break;
    case "huacRequiredByText":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/uacRequests/contactMadeHUACRequest.txt"), Charsets.UTF_8);
      break;
    case "iuacRequiredByText":
      testOutcomeJson = Resources
          .toString(Resources.getResource("files/outcome/uacRequests/contactMadeIUACRequest.txt"), Charsets.UTF_8);
      break;
    }
  }

  @And("the response is of a Census Case Outcome format")
  public void theResponseIsOfACensusCaseOutcomeFormat() {
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
        DateTimeFormatter.ISO_DATE_TIME);
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
    objectMapper = Jackson2ObjectMapperBuilder.json()
        .modules(module)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    try {
      objectMapper.readValue(testOutcomeJson.getBytes(), HouseholdOutcome.class);
    } catch (IOException e) {
      fail();
    }
  }

  @And("the response contains the Primary Outcome value of {string} and Secondary Outcome {string} and the Case Id of {string}")
  public void theResponseContainsThePrimaryOutcomeValueOfSecondaryOutcomeAndTheCaseIdOf(String primaryOutcome,
      String secondaryOutcome,
      String caseId) throws IOException {
    HouseholdOutcome householdOutcome = objectMapper.readValue(testOutcomeJson.getBytes(), HouseholdOutcome.class);
    assertEquals(primaryOutcome, householdOutcome.getPrimaryOutcome());
    assertEquals(secondaryOutcome, householdOutcome.getSecondaryOutcome());
    assertEquals(caseId, String.valueOf(householdOutcome.getCaseId()));
  }

  @Then("the message will made available for RM to pick up from queue {string}")
  public void theMessageWillMadeAvailableForRMToPickUpFromQueue(String queueName) {
    assertEquals(1, queueUtils.getMessageCount(queueName));
  }

  @And("the message is in the format RM is expecting from queue {string}")
  public void theMessageIsInTheFormatRMIsExpectingFromQueue(String queueName) {
    try {
      objectMapper.readValue(queueUtils.getMessage(queueName), OutcomeEvent.class);
    } catch (IOException | InterruptedException e) {
      fail();
    }
  }

  @Then("the number of messages {string} will made available for RM to pick up from queue {string}")
  public void theNumberOfMessagesWillMadeAvailableForRMToPickUpFromQueue(String expectedNumberOfMessages,
      String queueName) {
    long expectedNumber = Long.valueOf(expectedNumberOfMessages);
    assertEquals(expectedNumber, queueUtils.getMessageCount(queueName));
  }

  @And("the response contains the QuestionnaireId {string} from queue {string}")
  public void theResponseContainsTheQuestionnaireIdFromQueue(String questionnaireId, String queueName)
      throws IOException, InterruptedException {
    JavaTimeModule module = new JavaTimeModule();
    LocalDateTimeDeserializer localDateTimeDeserializer = new LocalDateTimeDeserializer(
        DateTimeFormatter.ISO_DATE_TIME);
    module.addDeserializer(LocalDateTime.class, localDateTimeDeserializer);
    objectMapper = Jackson2ObjectMapperBuilder.json()
        .modules(module)
        .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build();

    OutcomeEvent outcomeEvent = objectMapper.readValue(queueUtils.getMessage(queueName), OutcomeEvent.class);
    assertEquals(questionnaireId, outcomeEvent.getPayload().getUac().getQuestionnaireId());
  }
}
