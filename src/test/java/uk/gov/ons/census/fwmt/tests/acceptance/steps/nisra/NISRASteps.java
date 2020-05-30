package uk.gov.ons.census.fwmt.tests.acceptance.steps.nisra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeoutException;

import javax.xml.bind.JAXBException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import lombok.extern.slf4j.Slf4j;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.common.error.GatewayException;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

@Slf4j
@PropertySource("classpath:application.properties")
public class NISRASteps {

  private static final String RM_CREATE_REQUEST_RECEIVED = "RM_CREATE_REQUEST_RECEIVED";
  private static final String COMET_CREATE_ACK = "COMET_CREATE_ACK";
  private String nisraHouseholdMessage = null;
  private String nisraNoFieldOfficerMessage = null;

  @Autowired
  private TMMockUtils tmMockUtils;

  @Autowired
  private QueueClient queueUtils;

  private GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  @Value("${service.rabbit.url}")
  private String rabbitLocation;

  @Value("${service.rabbit.username}")
  private String rabbitUsername;

  @Value("${service.rabbit.password}")
  private String rabbitPassword;

  @Before
  public void setup() throws IOException, TimeoutException, URISyntaxException {
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

//  @Given("RM sends a create NISRA job request job which has a case ID of {string} and a field officer ID {string}")
//  public void rmSendsACreateHouseHoldJobRequestJobWhichHasACaseIDOfAndAFieldOfficerID(String caseId,
//      String fieldOfficerId)
//      throws URISyntaxException, InterruptedException, JAXBException {
//    JAXBElement<ActionInstruction> actionInstruction = tmMockUtils.unmarshalXml(nisraHouseholdMessage);
//    queueUtils.sendToRMFieldQueue(nisraHouseholdMessage);
//    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
//    assertEquals(fieldOfficerId, actionInstruction.getValue().getActionRequest().getFieldOfficerId());
//    assertThat(hasBeenTriggered).isTrue();
//  }

  @And("the Gateway sends a create NISRA Job message to TM with case ID of {string}")
  public void theGatewaySendsACreateJobMessageToTMWithCaseIdOf(String caseId) {
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, COMET_CREATE_ACK, 10000L);
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
    queueUtils.sendToRMFieldQueue(nisraNoFieldOfficerMessage, "update");
    boolean hasBeenTriggered = gatewayEventMonitor.hasEventTriggered(caseId, RM_CREATE_REQUEST_RECEIVED, 10000L);
    assertThat(hasBeenTriggered).isTrue();
  }

  @Then("RM will throw an exception for case ID {string} for NISRA")
  public void rmWillThrowAnExceptionForCaseID(String caseId) throws URISyntaxException, InterruptedException {
    queueUtils.sendToRMFieldQueue(nisraNoFieldOfficerMessage, "update");
    assertThatExceptionOfType(GatewayException.class).isThrownBy(() -> {
      throw new GatewayException(
          GatewayException.Fault.SYSTEM_ERROR);
    });
  }
}
