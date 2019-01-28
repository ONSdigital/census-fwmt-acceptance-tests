package uk.gov.ons.fwmt.census.tests.acceptance.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.fwmt.census.jobservice.data.dto.CensusCaseOutcomeDTO;
import uk.gov.ons.fwmt.census.tests.acceptance.utils.AcceptanceTestUtils;
import uk.gov.ons.fwmt.census.tests.acceptance.utils.MessageSenderUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@Slf4j
@PropertySource("classpath:application.properties")
public class CensusSteps {

  private String censusResponse = null;
  private String expectedXml = null;

  @Autowired
  private AcceptanceTestUtils acceptanceTestUtils;

  @Autowired
  private MessageSenderUtils ms;

  @Value("${service.mocktm.url}")
  private String mockTmURL;

  @Before
  public void reset() throws IOException, TimeoutException, URISyntaxException {
    censusResponse = Resources.toString(Resources.getResource("files/census_response.txt"), Charsets.UTF_8);
    expectedXml = Resources.toString(Resources.getResource("files/feedback.xml"), Charsets.UTF_8);
    
    acceptanceTestUtils.resetMock();
    acceptanceTestUtils.clearQueues();
  }

  @Given("^TM sends a Census case outcome to the Job Service$")
  public void tm_sends_a_LMS_case_outcome_to_the_Job_Service() throws Exception {
    int response = ms.sendTMResponseMessage(censusResponse);
    assertEquals(200, response);
  }

  @Given("^the response contains the outcome and caseId$")
  public void the_response_contains_the_outcome_and_caseId() throws JsonParseException, JsonMappingException, IOException {
    ObjectMapper ob = new ObjectMapper();
    CensusCaseOutcomeDTO dto = ob.readValue(censusResponse.getBytes(), CensusCaseOutcomeDTO.class);
    assertEquals("6c9b1177-3e03-4060-b6db-f6a8456292ef", dto.getCaseId());
    assertEquals("Will complete", dto.getOutcome());
    assertEquals("Will complete", dto.getOutcomeCategory());
  }

  @Then("^the response is an Census job$")
  public void the_response_is_a_Census_job() {
      ObjectMapper ob = new ObjectMapper();
      try {
        ob.readValue(censusResponse.getBytes(), CensusCaseOutcomeDTO.class);
      } catch (IOException e) {
        fail();
      }
  }

  @Then("^the message is in the RM composite format$")
  public void the_message_is_in_the_RM_composite_format() throws Exception {
    assertEquals(expectedXml, ms.getMessage("rm.feedback"));
  }

  @Then("^the message will be put on the queue to RM$")
  public void the_message_will_be_put_on_the_queue_to_RM() {
    assertEquals(1, ms.getMessageCount("rm.feedback"));
  }
}
