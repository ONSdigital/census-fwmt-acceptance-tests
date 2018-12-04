package uk.gov.ons.fwmt.censu.tests.acceptance.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.auth.AuthenticationException;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static uk.gov.ons.fwmt.fwmtgatewaycommon.config.QueueNames.ADAPTER_TO_RM_QUEUE;

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

  public int sendTMResponseMessage(String data) throws IOException, AuthenticationException {
    CloseableHttpClient client = HttpClients.createDefault();
    int resopnseCode;

    try {
      HttpPost httpPost = new HttpPost(jobSvcURL + tmResponseEndpoint);

      httpPost.setEntity(new StringEntity(data));
      UsernamePasswordCredentials creds
          = new UsernamePasswordCredentials(jobserviceUsername, jobservicePassword);
      httpPost.addHeader(new BasicScheme().authenticate(creds, httpPost, null));
      httpPost.addHeader("Content-type", "text/xml");

      CloseableHttpResponse response = client.execute(httpPost);
      resopnseCode = response.getStatusLine().getStatusCode();
    } finally {
      client.close();
    }

    return resopnseCode;
  }

  public long getMessageCount() {
    RestTemplate restTemplate = new RestTemplate();
    String messageCountUrl = mockTmURL + "/queue/count/" + ADAPTER_TO_RM_QUEUE;
    ResponseEntity<Long> messageCount = restTemplate.getForEntity(messageCountUrl, Long.class);
    return messageCount.getBody();
  }

  public String getMessage() throws InterruptedException {
    RestTemplate restTemplate = new RestTemplate();
    String messageUrl = mockTmURL + "/queue/message/?qname=" + ADAPTER_TO_RM_QUEUE;
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
}
