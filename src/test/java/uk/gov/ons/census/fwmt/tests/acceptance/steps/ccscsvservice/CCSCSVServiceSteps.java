package uk.gov.ons.census.fwmt.tests.acceptance.steps.ccscsvservice;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.bs.A;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.StorageUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@Slf4j
@PropertySource("classpath:application.properties")
public class CCSCSVServiceSteps {

  public static final String CSV_CCS_REQUEST_EXTRACTED = "CSV_CCS_REQUEST_EXTRACTED";
  public static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueUtils;

  @Autowired
  private CSVSerivceUtils csvSerivceUtils;

  @Autowired
  private StorageUtils storageUtils;

  @Value("${service.csvservice.gcpBucket.aclocation}")
  String location;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private String caseId;

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();
    String csvData = Resources
        .toString(Resources.getResource("files/csv/testCCSCSV.csv"), Charsets.UTF_8);
    storageUtils.createFile("ccsTest", csvData, location);


    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
  }

  @Given("the Gateway receives a CSV CCS")
  public void theGatewayReceivesACSVCCSWithCaseID() throws IOException, InterruptedException {
    Collection<GatewayEventDTO> message;

    csvSerivceUtils.enableCCSCsvService();

    message = gatewayEventMonitor.grabEventsTriggered(CSV_CCS_REQUEST_EXTRACTED, 1, 10000L);

    for (GatewayEventDTO retrieveCaseId : message) {
      caseId = retrieveCaseId.getCaseId();
    }

    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CSV_CCS_REQUEST_EXTRACTED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with new case id for job containing postcode {string} is created in TM")
  public void aNewCaseWithNewCaseIdForJobContainingPostcodeIsCreatedInTM(String postcode) throws InterruptedException {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
    assertThat(hasBeenTriggered).isTrue();

    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
    assertEquals(postcode, modelCase.getAddress().getPostcode());
  }
}
