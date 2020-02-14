package uk.gov.ons.census.fwmt.tests.acceptance.steps.cecsvservice;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

@Slf4j
@PropertySource("classpath:application.properties")
public class CECSVServiceSteps {

    private static final String CANONICAL_CREATE_SENT = "CANONICAL_CREATE_SENT";
    public static final String CSV_CE_REQUEST_EXTRACTED = "CSV_CE_REQUEST_EXTRACTED";
    private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";

    @Autowired
    private TMMockUtils tmMockUtils;

    @Autowired
    private QueueClient queueUtils;

    @Autowired
    private CSVSerivceUtils csvSerivceUtils;

    private GatewayEventMonitor gatewayEventMonitor;

    @Value("${service.mocktm.url}")
    private String mockTmUrl;

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

        gatewayEventMonitor = new GatewayEventMonitor();
        gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
    }

    @After
    public void tearDownGatewayEventMonitor() throws IOException {
        gatewayEventMonitor.tearDownGatewayEventMonitor();
        tmMockUtils.disableRequestRecorder();
    }

    @Given("the Gateway receives a CSV CE with case ID {string}")
    public void theGatewayReceivesACSVCEWithCaseID(String caseId) throws InterruptedException, IOException {
        csvSerivceUtils.enableCECsvService();
        boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CSV_CE_REQUEST_EXTRACTED, 10000L);
        assertThat(hasBeenTriggered).isTrue();
    }

    @When("the Gateway sends a CE Create Job message to TM with case ID {string}")
    public void theGatewaySendsACreateJobMessageToTMWithCaseID(String caseId) {
        boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CANONICAL_CREATE_SENT, 10000L);
        assertThat(hasBeenTriggered).isTrue();
    }

    @And("TM picks up the CE Create Job message with case ID {string}")
    public void tmPicksUpTheCreateJobMessageWithCaseID(String caseId) {
        boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
        assertThat(hasBeenTriggered).isTrue();
    }

    @Then("a new case with id of {string} is created in TM for the CE")
    public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
        ModelCase modelCase = tmMockUtils.getCaseById(caseId);
        assertEquals(caseId, modelCase.getId().toString());
    }
}

