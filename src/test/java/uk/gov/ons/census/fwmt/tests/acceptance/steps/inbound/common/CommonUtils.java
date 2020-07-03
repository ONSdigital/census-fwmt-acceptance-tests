package uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

@Component
public class CommonUtils {

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

    public static Map<String, String> testBucket = new HashMap<>();
  
    public void setup() throws Exception {  
      tmMockUtils.enableRequestRecorder();
      tmMockUtils.resetMock();
      tmMockUtils.clearDownDatabase();
  
      gatewayEventMonitor.enableEventMonitor(rabbitLocation, rabbitUsername, rabbitPassword);
    }
  
    public void clearDown() throws Exception {
        gatewayEventMonitor.tearDownGatewayEventMonitor();
      tmMockUtils.disableRequestRecorder();  
      queueUtils.clearQueues("RM.Field", "RM.FieldDLQ", "Outcome.Preprocessing", "Outcome.PreprocessingDLQ");
    }


}