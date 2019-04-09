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
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.data.dto.MockMessage;
import uk.gov.ons.census.fwmt.tests.acceptance.exceptions.MockInaccessibleException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
public final class TMMockUtils {

  @Value("${service.jobservice.url}")
  private String jobSvcURL;

  @Value("${service.jobservice.username}")
  private String jobserviceUsername;

  @Value("${service.jobservice.password}")
  private String jobservicePassword;

  @Value("${service.outcome.url}")
  private String outcomeSvcURL;

  @Value("${service.outcome.username}")
  private String outcomeSvcUsername;

  @Value("${service.outcome.password}")
  private String outcomeSvcPassword;

  @Value("${service.mocktm.url}")
  private String mockTmURL;

  @Value("${service.tmresponse.url}")
  private String tmResponseEndpoint;

  private RestTemplate restTemplate = new RestTemplate();

  public void resetMock() throws IOException {
    URL url = new URL(mockTmURL + "/logger/reset");
    log.info("reset-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }

  public MockMessage[] getMessages() {
    String url = mockTmURL + "/logger/allMessages";
    log.info("allMessages-mock_url:" + url);
    return restTemplate.getForObject(url, MockMessage[].class);
  }

  public ModelCase getCaseById(String id) {
    String url = mockTmURL + "/cases/" + id;
    log.info("getCaseById-mock_url:" + url);
    ResponseEntity<ModelCase> responseEntity;
    responseEntity = restTemplate.getForEntity(url, ModelCase.class);
    return responseEntity.getBody();
  }

  public int sendTMResponseMessage(String data) {
    HttpHeaders headers = createBasicAuthHeaders(outcomeSvcUsername, outcomeSvcPassword);

    headers.setContentType(MediaType.APPLICATION_JSON);
    
    RestTemplate restTemplate = new RestTemplate();
    String postUrl = outcomeSvcURL + tmResponseEndpoint;

    HttpEntity<String> post = new HttpEntity<>(data, headers);
    ResponseEntity<Void> response = restTemplate.exchange(postUrl, HttpMethod.POST, post, Void.class);

    return response.getStatusCode().value();
  }

  private HttpHeaders createBasicAuthHeaders(String username, String password) {
    HttpHeaders headers = new HttpHeaders();
    final String plainCreds = username + ":" + password;
    byte[] plainCredsBytes = plainCreds.getBytes();
    byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
    String base64Creds = new String(base64CredsBytes);
    headers.add("Authorization", "Basic " + base64Creds);
    return headers;
  }

  public void enableRequestRecorder() throws IOException {
    URL url = new URL(mockTmURL + "/logger/enableRequestRecorder");
    log.info("enableRequestRecorder-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }

  public void disableRequestRecorder() throws IOException {
    URL url = new URL(mockTmURL + "/logger/disableRequestRecorder");
    log.info("disableRequestRecorder-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }
}
