package uk.gov.ons.census.fwmt.tests.acceptance.steps;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.data.GatewayEventDTO;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


@Slf4j
@PropertySource("classpath:application.properties")
public class CCSSteps {

    private static final String CANONICAL_CREATE_SENT = "Canonical - Action Create Sent";
    public static final String CSV_CCS_REQUEST_EXTRACTED = "CSV Service - CCS Request extracted";

    @Autowired
    private TMMockUtils tmMockUtils;

    @Autowired
    private QueueUtils queueUtils;

    @Autowired
    private CSVSerivceUtils csvSerivceUtils;

    private GatewayEventMonitor gatewayEventMonitor;

    @Value("${service.mocktm.url}")
    private String mockTmUrl;

    @Value("${service.rabbit.url}")
    private String rabbitLocation;

    private String caseId;

    @Before
    public void setup() throws IOException, TimeoutException, URISyntaxException {

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

    @Given("the Gateway receives a CSV CCS")
    public void theGatewayReceivesACSVCCSWithCaseID() throws IOException, InterruptedException, URISyntaxException {
        Collection<GatewayEventDTO> message;

        csvSerivceUtils.enableCsvService();

        message = gatewayEventMonitor.grabEventsTriggered("CSV Service - CCS Request extracted", 1, 10000L);

        for (GatewayEventDTO retrieveCaseId : message) {
            caseId = retrieveCaseId.getCaseId();
        }

        boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CSV_CCS_REQUEST_EXTRACTED, 10000L);
        assertThat(hasBeenTriggered).isTrue();
    }

    @Then("a new case with new case id for job containing postcode {string} is created in TM")
    public void aNewCaseWithNewCaseIdForJobContainingPostcodeIsCreatedInTM(String postcode) throws InterruptedException {
        ModelCase modelCase = tmMockUtils.getCaseById(caseId);
        assertEquals(caseId, modelCase.getId().toString());
        assertEquals(postcode, modelCase.getAddress().getPostcode());
    }
}
