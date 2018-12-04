package uk.gov.ons.fwmt.census.tests.acceptance.steps;

import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.fwmt.censu.tests.acceptance.utils.AcceptanceTestUtils;
import uk.gov.ons.fwmt.censu.tests.acceptance.utils.MessageSenderUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;

@Slf4j
@PropertySource("classpath:application.properties")
public class CensusSteps {

  public static final String CENSUS_RESPONSE = "<?xml version=\"1.0\" encoding=\"utf-8\"?><soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><soap:Body><request xmlns=\"http://schemas.consiliumtechnologies.com/services/mobile/2009/03/messaging\"><Id>IDENTITY</Id><Content>&lt;![CDATA[&lt;?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?&gt;&lt;ns2:FwmtOHSJobStatusNotification xmlns:ns2=\"http://ons.gov.uk/fwmt/FwmtOHSJobStatusNotification\"&gt;    &lt;eventDate&gt;2018-10-29T08:03:40.269Z&lt;/eventDate&gt;    &lt;jobIdentity&gt;1000000000431094&lt;/jobIdentity&gt;    &lt;nonContactDetail/&gt;    &lt;outcomeCategory&gt;Will complete&lt;/outcomeCategory&gt;    &lt;outcomeReason&gt;Will complete&lt;/outcomeReason&gt;    &lt;propertyDetails&gt;        &lt;description&gt;No physical impediments or barriers&lt;/description&gt;    &lt;/propertyDetails&gt;    &lt;username&gt;USERNAME&lt;/username&gt;    &lt;additionalProperties&gt;        &lt;name&gt;caseId&lt;/name&gt;        &lt;value&gt;CASEID&lt;/value&gt;    &lt;/additionalProperties&gt;    &lt;additionalProperties&gt;        &lt;name&gt;TLA&lt;/name&gt;        &lt;value&gt;CENSUS&lt;/value&gt;    &lt;/additionalProperties&gt;    &lt;additionalProperties&gt;        &lt;name&gt;wave&lt;/name&gt;        &lt;value&gt;1&lt;/value&gt;    &lt;/additionalProperties&gt;&lt;/ns2:FwmtOHSJobStatusNotification&gt;]]&gt;</Content><Format>fwmtOHSJobStatusNotification</Format></request></soap:Body></soap:Envelope>";
  private static final String EXPECTED_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><ns2:FwmtOHSJobStatusNotification xmlns:ns2=\"http://ons.gov.uk/fwmt/FwmtOHSJobStatusNotification\"><eventDate>2018-10-29T08:03:40.269Z</eventDate><jobIdentity>1000000000431094</jobIdentity><nonContactDetail/><outcomeCategory>Will complete</outcomeCategory><outcomeReason>Will complete</outcomeReason><propertyDetails><description>No physical impediments or barriers</description></propertyDetails><username>USERNAME</username><additionalProperties><name>caseId</name><value>CASEID</value></additionalProperties><additionalProperties><name>TLA</name><value>CENSUS</value></additionalProperties><additionalProperties><name>wave</name><value>1</value></additionalProperties></ns2:FwmtOHSJobStatusNotification>";

  @Autowired
  private AcceptanceTestUtils acceptanceTestUtils;

  @Autowired
  private MessageSenderUtils ms;

  @Value("${service.mocktm.url}")
  private String mockTmURL;

  @Before
  public void reset() throws IOException, TimeoutException, URISyntaxException {
    acceptanceTestUtils.resetMock();
    acceptanceTestUtils.clearQueues();
  }

  @Given("^TM sends a Census case outcome to the Job Service$")
  public void tm_sends_a_LMS_case_outcome_to_the_Job_Service() throws Exception {
    int response = ms.sendTMResponseMessage(CENSUS_RESPONSE);
    assertEquals(200, response);
  }

  @Given("^the response contains the outcome and caseId$")
  public void the_response_contains_the_outcome_and_caseId() {
    assertTrue(CENSUS_RESPONSE.contains("&lt;name&gt;caseId&lt;/name&gt;        &lt;value&gt;CASEID&lt;/value&gt;"));
    assertTrue(CENSUS_RESPONSE.contains("&lt;outcomeCategory&gt;Will complete&lt;/outcomeCategory&gt;"));
    assertTrue(CENSUS_RESPONSE.contains("&lt;outcomeReason&gt;Will complete&lt;/outcomeReason&gt;"));
  }

  @Then("^the response is an Census job$")
  public void the_response_is_a_Census_job() {
    assertTrue(CENSUS_RESPONSE.contains("&lt;value&gt;CENSUS&lt;/value&gt;"));
  }

  @Then("^the message is in the RM composite format$")
  public void the_message_is_in_the_RM_composite_format() throws Exception {
    assertTrue(ms.getMessage().equals(EXPECTED_XML));
  }

  @Then("^the message will be put on the queue to RM$")
  public void the_message_will_be_put_on_the_queue_to_RM() {
    assertEquals(1, ms.getMessageCount());
  }
}
