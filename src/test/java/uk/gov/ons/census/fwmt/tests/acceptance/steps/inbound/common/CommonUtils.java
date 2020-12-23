package uk.gov.ons.census.fwmt.tests.acceptance.steps.inbound.common;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.ons.census.fwmt.events.utils.GatewayEventMonitor;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.PreFlightCheck;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.QueueClient;
import uk.gov.ons.census.fwmt.tests.acceptance.utils.TMMockUtils;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommonUtils {

    public final static long TIMEOUT = 10000L;

    @Autowired
    private TMMockUtils tmMockUtils;

    @Autowired
    private QueueClient queueClients;

    @Autowired
    private GatewayEventMonitor gatewayEventMonitor;
    
    @Autowired
    private PreFlightCheck preFlightCheck;

    @Value("${service.rabbit.gw.url}")
    private String rabbitGWLocation;

    @Value("${service.rabbit.gw.username}")
    private String rabbitGWUsername;

    @Value("${service.rabbit.gw.password}")
    private String rabbitGWPassword;

    @Value("${service.rabbit.gw.port:5673}")
    private int rabbitmqGWPort;

    public static Map<String, String> testBucket = new HashMap<>();

    public void setup() throws Exception {
      preFlightCheck.doCheck();
      tmMockUtils.enableRequestRecorder();
      tmMockUtils.resetMock();
      tmMockUtils.clearDownDatabase();
      queueClients.createQueue();
      
      gatewayEventMonitor.enableEventMonitor(rabbitGWLocation, rabbitGWUsername, rabbitGWPassword, rabbitmqGWPort);
      gatewayEventMonitor.reset();
      queueClients.reset();
    }

    public void clearDown() throws Exception {
      gatewayEventMonitor.tearDownGatewayEventMonitor();
      tmMockUtils.disableRequestRecorder();
    }





}