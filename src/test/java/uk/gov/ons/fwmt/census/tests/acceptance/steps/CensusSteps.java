package uk.gov.ons.fwmt.census.tests.acceptance.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.fwmt.census.jobservice.data.dto.CensusCaseOutcomeDTO;
import uk.gov.ons.fwmt.census.tests.acceptance.utils.AcceptanceTestUtils;
import uk.gov.ons.fwmt.census.tests.acceptance.utils.GatewayEventMonitor;
import uk.gov.ons.fwmt.census.tests.acceptance.utils.MessageSenderUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class CensusSteps {

  private String censusResponse = null;
  private String expectedXml = null;
  private String unexppectedXml = null;
  private String receivedRMMessage = null;
  private String invalidRMMessage = null;

  @Autowired
  private AcceptanceTestUtils acceptanceTestUtils;

  @Autowired
  private MessageSenderUtils messageSenderUtils;

  @Autowired
  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmURL;

  @Before
  public void reset() throws IOException, TimeoutException, URISyntaxException {
    censusResponse = Resources.toString(Resources.getResource("files/census_response.txt"), Charsets.UTF_8);
    expectedXml = Resources.toString(Resources.getResource("files/feedback.xml"), Charsets.UTF_8);
    unexppectedXml = Resources.toString(Resources.getResource("files/badFeedback.xml"), Charsets.UTF_8);
    receivedRMMessage = Resources.toString(Resources.getResource("files/actionInstruction.xml"), Charsets.UTF_8);
    invalidRMMessage = Resources.toString(Resources.getResource("files/invalidInstruction.xml"), Charsets.UTF_8);
    
    acceptanceTestUtils.resetMock();
    acceptanceTestUtils.clearQueues();
    gatewayEventMonitor.enableEventMonitor();
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
  }

  @Given("^TM sends a Census case outcome to the Job Service$")
  public void tm_sends_a_LMS_case_outcome_to_the_Job_Service() {
    int response = messageSenderUtils.sendTMResponseMessage(censusResponse);
    assertEquals(200, response);
  }

  @Given("^the response contains the outcome and caseId$")
  public void the_response_contains_the_outcome_and_caseId() throws IOException {
    ObjectMapper ob = new ObjectMapper();
    CensusCaseOutcomeDTO dto = ob.readValue(censusResponse.getBytes(), CensusCaseOutcomeDTO.class);
    assertEquals("6c9b1177-3e03-4060-b6db-f6a8456292ef", dto.getCaseId());
    assertEquals("Will complete", dto.getOutcome());
    assertEquals("Will complete", dto.getOutcomeCategory());
  }

  @Then("^the response is an Census job$")
  public void the_response_is_a_Census_job() {
    ObjectMapper objectMapper = new ObjectMapper();
      try {
        objectMapper.readValue(censusResponse.getBytes(), CensusCaseOutcomeDTO.class);
      } catch (IOException e) {
        fail();
      }
  }

  @Then("^the message is in the RM composite format$")
  public void the_message_is_in_the_RM_composite_format() throws Exception {
    assertEquals(expectedXml, messageSenderUtils.getMessage("rm.feedback"));
  }

  @Then("^the message will be put on the queue to RM$")
  public void the_message_will_be_put_on_the_queue_to_RM() {
    assertEquals(1, messageSenderUtils.getMessageCount("rm.feedback"));
  }

  @Given("a job with the id {string} doesn't exist")
  public void aJobWithTheIdDoesnTExist(String id) {
    acceptanceTestUtils.getCaseById(id);
  }

  @And("RM sends a create job request")
  public void rm_sends_a_create_job_request() throws URISyntaxException, InterruptedException {
    messageSenderUtils.sendToRMQueue(receivedRMMessage);
    assertTrue(messageSenderUtils.hasEventTriggered("caseId eventType"));
  }

  @When("the gateway sends a create message to TM")
  public void the_gateway_sends_a_create_message_to_TM() {
    assertTrue(messageSenderUtils.hasEventTriggered("caseId eventType"));
  }

  @Then("a new case is created in TM")
  public void a_new_case_is_created_in_TM() {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  @Given("a message in an invalid format from RM")
  public void a_message_in_an_invalid_format_from_RM() throws URISyntaxException, InterruptedException {
    messageSenderUtils.sendToRMQueue(invalidRMMessage);
  }

  @Given("a message in an invalid format from TM")
  public void a_message_in_an_invalid_format_from_TM() {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  @Given("a message received from RM that fails to send to TM after {int} attempts")
  public void a_message_received_from_RM_that_fails_to_send_to_TM_after_attempts(Integer attempts) {
    // Write code here that turns the phrase above into concrete actions
    throw new cucumber.api.PendingException();
  }

  // Shared step
  @Then("the error is logged via SPLUNK & stored in a queue {string}")
  public void theErrorIsLoggedViaSPLUNKStoredInA(String queueName) {
    assertEquals(1, messageSenderUtils.getMessageCount(queueName));
  }
}
