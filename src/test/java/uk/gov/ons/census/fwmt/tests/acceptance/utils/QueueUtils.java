package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
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

@Slf4j
@Component
public final class QueueUtils {
  @Value("${service.mocktm.url}")
  private String mockTmURL;
  
  private RestTemplate restTemplate = new RestTemplate();

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

  public void sendToActionFieldQueue(String message) throws URISyntaxException, InterruptedException {
    Thread.sleep(3000); // TODO do we need this thread sleep?
    String exchangeName = "";
    String routingKey = "Action.Field";
    RestTemplate rt = new RestTemplate();
    HttpEntity<String> httpEntity = new HttpEntity<>(message);
    URI uri = new URI(mockTmURL + "/queue/?exchange=" + exchangeName + "&routingkey=" + routingKey);
    rt.postForLocation(uri, httpEntity);
  }
  
  public void clearQueues() throws URISyntaxException {
    clearQueue("Gateway.Actions");
    clearQueue("Gateway.ActionsDLQ");
    clearQueue("Gateway.Outcome");
    clearQueue("Action.Field");
    clearQueue("Action.FieldDLQ");
  }

  public void clearQueue(String queueName) throws URISyntaxException {
    URI uri = new URI(mockTmURL + "/queue/?qname=" + queueName);
    restTemplate.delete(uri);
  }

}
