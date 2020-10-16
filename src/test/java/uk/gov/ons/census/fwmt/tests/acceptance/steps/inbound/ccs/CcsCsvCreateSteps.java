package uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.ccs;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.util.Collection;

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

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private String caseId;

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
