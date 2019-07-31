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
import uk.gov.ons.census.fwmt.common.data.modelcase.CasePause;
import uk.gov.ons.census.fwmt.common.data.modelcase.ModelCase;
import uk.gov.ons.census.fwmt.data.dto.MockMessage;
import uk.gov.ons.census.fwmt.tests.acceptance.exceptions.MockInaccessibleException;
import uk.gov.ons.ctp.response.action.message.instruction.ActionInstruction;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@Component
public final class TMMockUtils {

  @Value("${service.jobservice.url}")
  private String jobServiceUrl;

  @Value("${service.jobservice.username}")
  private String jobServiceUsername;

  @Value("${service.jobservice.password}")
  private String jobServicePassword;

  @Value("${service.outcome.url}")
  private String outcomeServiceUrl;

  @Value("${service.outcome.household.endpoint}")
  private String householdOutcomeEndpoint;

  @Value("${service.outcome.CCSPL.endpoint}")
  private String ccsPLOutcomeEnpoint;

  @Value("${service.outcome.CCSInt.endpoint}")
  private String ccsIntOutcomeEnpoint;

  @Value("${service.outcome.username}")
  private String outcomeServiceUsername;

  @Value("${service.outcome.password}")
  private String outcomeServicePassword;

  @Value("${service.mocktm.url}")
  private String mockTmUrl;

  private RestTemplate restTemplate = new RestTemplate();

  private JAXBContext jaxbContext;

  public void resetMock() throws IOException {
    URL url = new URL(mockTmUrl + "/logger/reset");
    log.info("reset-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }

  public MockMessage[] getMessages() {
    String url = mockTmUrl + "/logger/allMessages";
    log.info("allMessages-mock_url:" + url);
    return restTemplate.getForObject(url, MockMessage[].class);
  }

  public ModelCase getCaseById(String id) {
    String url = mockTmUrl + "/cases/" + id;
    log.info("getCaseById-mock_url:" + url);
    ResponseEntity<ModelCase> responseEntity;
    responseEntity = restTemplate.getForEntity(url, ModelCase.class);
    return responseEntity.getBody();
  }

  public CasePause getPauseCase(String id) {
    String url = mockTmUrl + "/cases/" + id + "/pause";
    log.info("getCancelCaseById-mock.url:" + url);
    ResponseEntity<CasePause> responseEntity;
    responseEntity = restTemplate.getForEntity(url, CasePause.class);
    return responseEntity.getBody();
  }

  public int sendTMResponseMessage(String data, String caseId) {
    HttpHeaders headers = createBasicAuthHeaders(outcomeServiceUsername, outcomeServicePassword);

    headers.setContentType(MediaType.APPLICATION_JSON);

    RestTemplate restTemplate = new RestTemplate();
    String postUrl = outcomeServiceUrl + householdOutcomeEndpoint;

    HttpEntity<String> post = new HttpEntity<>(data, headers);
    ResponseEntity<Void> response = restTemplate.exchange(postUrl, HttpMethod.POST, post, Void.class);

    return response.getStatusCode().value();
  }

  public int sendTMCCSPLResponseMessage(String data, String caseId) {
    HttpHeaders headers = createBasicAuthHeaders(outcomeServiceUsername, outcomeServicePassword);

    headers.setContentType(MediaType.APPLICATION_JSON);

    RestTemplate restTemplate = new RestTemplate();
    String postUrl = outcomeServiceUrl + ccsPLOutcomeEnpoint;

    HttpEntity<String> post = new HttpEntity<>(data, headers);
    ResponseEntity<Void> response = restTemplate.exchange(postUrl, HttpMethod.POST, post, Void.class);

    return response.getStatusCode().value();
  }

  public int sendTMCCSIntResponseMessage(String data, String caseId) {
    HttpHeaders headers = createBasicAuthHeaders(outcomeServiceUsername, outcomeServicePassword);

    headers.setContentType(MediaType.APPLICATION_JSON);

    RestTemplate restTemplate = new RestTemplate();
    String postUrl = outcomeServiceUrl + ccsIntOutcomeEnpoint + caseId;

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
    URL url = new URL(mockTmUrl + "/logger/enableRequestRecorder");
    log.info("enableRequestRecorder-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }

  public void disableRequestRecorder() throws IOException {
    URL url = new URL(mockTmUrl + "/logger/disableRequestRecorder");
    log.info("disableRequestRecorder-mock_url:" + url.toString());
    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
    httpURLConnection.setRequestMethod("GET");
    if (httpURLConnection.getResponseCode() != 200) {
      throw new MockInaccessibleException("Failed : HTTP error code : " + httpURLConnection.getResponseCode());
    }
  }

  public JAXBElement<ActionInstruction> unmarshalXml(String message) throws JAXBException {
    jaxbContext = JAXBContext.newInstance(ActionInstruction.class);
    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
    ByteArrayInputStream input = new ByteArrayInputStream(message.getBytes());
    JAXBElement<ActionInstruction> rmActionInstruction = unmarshaller
        .unmarshal(new StreamSource(input), ActionInstruction.class);
    return rmActionInstruction;
  }
}
