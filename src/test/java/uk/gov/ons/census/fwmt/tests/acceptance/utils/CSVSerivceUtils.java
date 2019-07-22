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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@Slf4j
@Component
public class CSVSerivceUtils {
    @Value("${service.ccscsv.url}")
    private String ccsCsvService;

    @Value("${service.cecsv.url}")
    private String ceCsvService;

    @Value("${service.csvservice.username}")
    private String csvServiceUsername;

    @Value("${service.csvservice.password}")
    private String csvServicePassword;

    @Value("${service.tm.url}")
    private String tmUrl;

    public int enableCCSCsvService() throws IOException, InterruptedException {
        HttpHeaders headers = createBasicAuthHeaders(csvServiceUsername, csvServicePassword);

        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> get = new HttpEntity<>(null, headers);
        ResponseEntity<Void> response = restTemplate.exchange(ccsCsvService, HttpMethod.GET, get, Void.class);

        return response.getStatusCode().value();
    }

    public int enableCECsvService() throws IOException, InterruptedException {
        HttpHeaders headers = createBasicAuthHeaders(csvServiceUsername, csvServicePassword);

        headers.setContentType(MediaType.APPLICATION_JSON);

        RestTemplate restTemplate = new RestTemplate();

        HttpEntity<String> get = new HttpEntity<>(null, headers);
        ResponseEntity<Void> response = restTemplate.exchange(ceCsvService, HttpMethod.GET, get, Void.class);

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
}
