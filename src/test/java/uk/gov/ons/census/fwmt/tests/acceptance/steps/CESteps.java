package uk.gov.ons.census.fwmt.tests.acceptance.steps;

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


@Slf4j
@PropertySource("classpath:application.properties")
public class CESteps {

    private static final String CANONICAL_CREATE_SENT = "Canonical - Action Create Sent";
    public static final String CSV_CE_REQUEST_EXTRACTED = "CSV Service - CE Request extracted";
    private static final String COMET_CREATE_JOB_REQUEST = "Comet - Create Job Request";

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

    @Given("the Gateway receives a CSV CE with case ID {string}")
    public void theGatewayReceivesACSVCEWithCaseID(String caseId) throws InterruptedException, IOException {
        csvSerivceUtils.enableCECsvService();
        boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, CSV_CE_REQUEST_EXTRACTED, 10000L);
        assertThat(hasBeenTriggered).isTrue();
    }
}

