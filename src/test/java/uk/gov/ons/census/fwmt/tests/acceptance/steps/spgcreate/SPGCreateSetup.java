package uk.gov.ons.census.fwmt.tests.acceptance.steps.spgcreate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import cucumber.api.java.After;
import cucumber.api.java.Before;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;


public class SPGCreateSetup {

    @Autowired
    private TMMockUtils tmMockUtils;
  
    @Autowired
    private QueueClient queueUtils;
  
    @Autowired
    private GatewayEventMonitor gatewayEventMonitor;

    @Value("${service.rabbit.url}")
    private String rabbitLocation;
  
    @Value("${service.rabbit.username}")
    private String rabbitUsername;
  
    @Value("${service.rabbit.password}")
    private String rabbitPassword;
  
  
    @Before
    public void setup() throws Exception {  
      tmMockUtils.enableRequestRecorder();
      tmMockUtils.resetMock();
      tmMockUtils.clearDownDatabase();
  
      //gatewayEventMonitor = new GatewayEventMonitor();
      gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
    }
  
    @After
    public void tearDownGatewayEventMonitor() throws Exception {
//      try{
        gatewayEventMonitor.tearDownGatewayEventMonitor();
//      }catch(Exception e){
//        System.out.println(e);
//      }
      tmMockUtils.disableRequestRecorder();
  
      queueUtils.clearQueues("RM.Field", "RM.FieldDLQ", "Outcome.Preprocessing", "Outcome.PreprocessingDLQ");
    }


}