package uk.gov.ons.census.fwmt.tests.acceptance.steps.nisra;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.CSVSerivceUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueUtils;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

@Slf4j
@PropertySource("classpath:application.properties")
public class NISRASteps {

  private static final String RM_REQUEST_RECEIVED = "RM - Request Received";
  private static final String COMET_CREATE_JOB_REQUEST = "Comet - Create Job Request";
  private String cancelMessage = null;
  private String cancelMessageNonHH = null;
  private String invalidRMMessage = null;
  private String nisraHouseholdMessage = null;
  private String nisraNoFieldOfficerMessage = null;
  private String receivedRMMessage = null;
  private String updateMessage = null;
  private String updatePauseMessage = null;

  @Autowired
  private CSVSerivceUtils csvServiceUtils;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueUtils queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  private ObjectMapper objectMapper = new ObjectMapper();

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
    cancelMessage = Resources
        .toString(Resources.getResource("files/input/actionCancelInstruction.xml"), Charsets.UTF_8);
    cancelMessageNonHH = Resources
        .toString(Resources.getResource("files/input/actionNonHHCancelInstruction.xml"), Charsets.UTF_8);
    invalidRMMessage = Resources.toString(Resources.getResource("files/input/invalidInstruction.xml"), Charsets.UTF_8);
    receivedRMMessage = Resources.toString(Resources.getResource("files/input/actionInstruction.xml"), Charsets.UTF_8);
    updateMessage = Resources.toString(Resources.getResource("files/input/actionUpdateInstruction.xml"), Charsets.UTF_8);
    updatePauseMessage = Resources.toString(Resources.getResource("files/input/actionUpdatePauseInstruction.xml"), Charsets.UTF_8);
    nisraNoFieldOfficerMessage = Resources.toString(Resources.getResource("files/input/nisraNoFieldOfficerActionInstruction.xml"), Charsets.UTF_8);
    nisraHouseholdMessage = Resources.toString(Resources.getResource("files/input/nisraActionInstruction.xml"), Charsets.UTF_8);

    tmMockUtils.enableRequestRecorder();
    tmMockUtils.resetMock();
    queueUtils.clearQueues();


    gatewayEventMonitor = new GatewayEventMonitor();
    gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
  }

  @After
  public void tearDownGatewayEventMonitor() throws IOException, TimeoutException {
    gatewayEventMonitor.tearDownGatewayEventMonitor();
    tmMockUtils.disableRequestRecorder();
  }

  @Given("RM sends a create NISRA job request job which has a case ID of {string} and a field officer ID {string}")
  public void rmSendsACreateHouseHoldJobRequestJobWhichHasACaseIDOfAndAFieldOfficerID(String caseId,
      String fieldOfficerId)
      throws URISyntaxException, InterruptedException, JAXBException {
    JAXBElement<ActionInstruction> actionInstruction = tmMockUtils.unmarshalXml(nisraHouseholdMessage);
    queueUtils.sendToRMFieldQueue(nisraHouseholdMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_REQUEST_RECEIVED, 10000L);
    assertEquals(fieldOfficerId, actionInstruction.getValue().getActionRequest().getFieldOfficerId());
    assertThat(hasBeenTriggered).isTrue();
  }

  @When("the Gateway sends a create NISRA Job message to TM with case ID of {string}")
  public void theGatewaySendsACreateJobMessageToTMWithCaseIdOf(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_JOB_REQUEST, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("a new case with id of {string} is created in TM for NISRA")
  public void aNewCaseIsCreatedInTm(String caseId) throws InterruptedException {
    ModelCase modelCase = tmMockUtils.getCaseById(caseId);
    assertEquals(caseId, modelCase.getId().toString());
  }

  @Given("RM sends a create NISRA job request job which has a case ID of {string}")
  public void rmSendsACreateHouseHoldJobRequestJobWhichHasACaseIDOfAndAnID(String caseId)
      throws URISyntaxException, InterruptedException, JAXBException {
    queueUtils.sendToRMFieldQueue(nisraNoFieldOfficerMessage);
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("RM will throw an exception for case ID {string} for NISRA")
  public void rmWillThrowAnExceptionForCaseID(String caseId) throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(nisraNoFieldOfficerMessage);
    assertThatExceptionOfType(GatewayException.class).isThrownBy(() -> {
      throw new GatewayException(
          GatewayException.Fault.SYSTEM_ERROR);
    });
  }
}
