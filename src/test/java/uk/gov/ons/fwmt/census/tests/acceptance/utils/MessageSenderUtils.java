package uk.gov.ons.fwmt.census.tests.acceptance.utils;

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
public class MessageSenderUtils {

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
    HttpHeaders headers = createBasicAuthHeaders(jobserviceUsername, jobservicePassword);

    headers.setContentType(MediaType.APPLICATION_JSON);
    
    RestTemplate restTemplate = new RestTemplate();
    String postUrl = jobSvcURL + tmResponseEndpoint;

    HttpEntity<String> post = new HttpEntity<String>(data, headers);
    ResponseEntity<Void> response = restTemplate.exchange(postUrl, HttpMethod.POST, post, Void.class);

    return response.getStatusCode().value();
  }

  public HttpHeaders createBasicAuthHeaders(String username, String password) {
    HttpHeaders headers = new HttpHeaders();
    final String plainCreds = username + ":" + password;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);
    headers.add("Authorization", "Basic " + base64Creds);
    return headers;
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
    Thread.sleep(3000); // TODO do we need this thread sleep?
    String exchangeName = "";
    String routingKey = "Action.Field";
    RestTemplate rt = new RestTemplate();
    HttpEntity<String> httpEntity = new HttpEntity<>(message);
    URI uri = new URI(mockTmURL + "/queue/?exchange=" + exchangeName + "&routingkey=" + routingKey);
    rt.postForLocation(uri, httpEntity);
  }
}
