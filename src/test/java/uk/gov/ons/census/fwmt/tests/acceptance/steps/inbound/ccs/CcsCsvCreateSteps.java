package uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.ccs;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.census.ffa.storage.utils.StorageUtils;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class CcsCsvCreateSteps {

  private static final String CSV_CCS_REQUEST_EXTRACTED = "CSV_CCS_REQUEST_EXTRACTED";
  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueUtils;

  @Autowired
  private CSVSerivceUtils csvSerivceUtils;

  @Autowired
  private StorageUtils storageUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.rabbit.gw.url}")
  private String rabbitGWLocation;

  @Value("${service.rabbit.gw.username}")
  private String rabbitGWUsername;

  @Value("${service.rabbit.gw.password}")
  private String rabbitGWPassword;

  @Value("${service.rabbit.gw.port:5673}")
  private int rabbitmqGWPort;

  @Value("${service.csvservice.gcpBucket.directory.output}")
  private String outputDirectory;

  @Value("${service.csvservice.gcpBucket.directory}")
  private String inputDirectory;

  private String caseId;

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearRMQueues();
    List<URI> ccsOutputFiles = storageUtils.getFilenamesInFolder(URI.create(outputDirectory), "CCS");
    storageUtils.move(ccsOutputFiles.get(0), URI.create(inputDirectory + "ccsTestCSV.csv"));

    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitGWLocation, rabbitGWUsername, rabbitGWPassword, rabbitmqGWPort);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
  }

  @Given("the Gateway receives a CSV CCS")
  public void theGatewayReceivesACSVCCSWithCaseID() {
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
