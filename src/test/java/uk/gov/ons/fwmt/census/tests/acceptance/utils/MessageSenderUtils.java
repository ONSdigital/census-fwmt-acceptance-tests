package uk.gov.ons.fwmt.census.tests.acceptance.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Set;

@Slf4j
@Component
public class MessageSenderUtils {

  @Autowired
  GatewayEventMonitor gatewayEventMonitor;

  @Value("${service.jobservice.username}")
  private String jobserviceUsername;

  @Value("${service.jobservice.password}")
  private String jobservicePassword;

  @Value("${service.jobservice.url}")
  private String jobSvcURL;

  @Value("${service.tmresponse.url}")
  private String tmResponseEndpoint;

  @Value("${service.mocktm.url}")
  private String mockTmURL;

  public int sendTMResponseMessage(String data) {
    HttpHeaders headers = new HttpHeaders();
    final String plainCreds = jobserviceUsername + ":" + jobservicePassword;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);
    headers.add("Authorization", "Basic " + base64Creds);
    headers.setContentType(MediaType.APPLICATION_JSON);
    
    RestTemplate restTemplate = new RestTemplate();
    String postUrl = jobSvcURL + tmResponseEndpoint;

    HttpEntity<String> post = new HttpEntity<String>(data, headers);
    ResponseEntity<Void> response = restTemplate.exchange(postUrl, HttpMethod.POST, post, Void.class);

    return response.getStatusCode().value();
  }

  public long getMessageCount(String qname) {
    RestTemplate restTemplate = new RestTemplate();
    String messageCountUrl = mockTmURL + "/queue/count/" + qname;
    ResponseEntity<Long> messageCount = restTemplate.getForEntity(messageCountUrl, Long.class);
    return messageCount.getBody();
  }

  public String getMessage(String qname) throws InterruptedException {
    RestTemplate restTemplate = new RestTemplate();
    String messageUrl = mockTmURL + "/queue/message/?qname=" + qname;
    String message = null;
    for (int i = 0; i < 10; i++) {
      ResponseEntity<String> messageEntity = restTemplate.getForEntity(messageUrl, String.class);
      message = messageEntity.getBody();

      if (message != null) {
        break;
      }
      Thread.sleep(500);
    }
    return message;
  }

  public void sendToRMQueue(String message) throws URISyntaxException, InterruptedException {
    Thread.sleep(3000);
    //    String exchangeName = "action-outbound-exchange";
    //    String routingKey = "Action.Field.binding";
    String exchangeName = "rm-jobsvc-exchange";
    String routingKey = "Action.Field";
    RestTemplate rt = new RestTemplate();
    HttpEntity<String> httpEntity = new HttpEntity<>(message);
    URI uri = new URI(mockTmURL + "/queue/?exchange=" + exchangeName + "&routingkey=" + routingKey);
    rt.postForLocation(uri, httpEntity);
  }

  public boolean hasEventTriggered(String receivedRMMessage) {
    Date startTime = new Date();
    boolean keepChecking = true;
    boolean isFound = false;

    while (keepChecking) {
      isFound = gatewayEventMonitor.getEventMap().keySet().contains(receivedRMMessage);
      Date now = new Date();
      long timeElapsed = now.getTime() - startTime.getTime();
      if (isFound || timeElapsed > 10000) {
        keepChecking = false;
      } else {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
    if (isFound == false) {
      log.info("Searcjing for key:" + receivedRMMessage + " in :-");
      Set<String> keys = gatewayEventMonitor.getEventMap().keySet();
      for (String key : keys) {
        log.info(key);
      }
    }
    return isFound;
  }
}