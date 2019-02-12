package uk.gov.ons.fwmt.census.tests.acceptance.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import uk.gov.ons.fwmt.census.common.data.modelcase.ModelCase;
import uk.gov.ons.fwmt.census.tests.acceptance.exceptions.MockInaccessibleException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

@Slf4j
@Component
public final class AcceptanceTestUtils {

  @Value("${service.mocktm.url}")
  private String mockTmURL;

  private RestTemplate restTemplate = new RestTemplate();

  public void clearQueues() throws URISyntaxException {
    clearQueue("Gateway.Actions");
    clearQueue("Gateway.ActionsDLQ");
    clearQueue("Gateway.Feedback");
    clearQueue("Action.Field");
    clearQueue("Action.FieldDLQ");
  }

  public void clearQueue(String queueName) throws URISyntaxException {
    URI uri = new URI(mockTmURL + "/queue/?qname=" + queueName);
    restTemplate.delete(uri);
  }

  public void resetMock() throws IOException {
    URL url = new URL(mockTmURL + "/logger/reset");
    log.info("reset-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }

  public MockMessage[] getMessages() throws IOException {
    URL url = new URL(mockTmURL + "/logger/allMessages");
    log.info("allMessages-mock_url:" + url.toString());
    return restTemplate.getForObject(mockTmURL + "/logger/allMessages", MockMessage[].class);
  }

  public int getCaseById(String id) throws MalformedURLException {
    URL url = new URL(mockTmURL + "/cases/" + id);
    log.info("getCaseById-mock_url:" + url.toString());
    ResponseEntity responseEntity = null;
    try {
      responseEntity = restTemplate.getForEntity(mockTmURL + "/cases/casesByIdGet/" + id, ModelCase.class);
    } catch (HttpClientErrorException e) {
      return 404;
    }
    return responseEntity.getStatusCodeValue();
  }
}
