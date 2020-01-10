package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Acl.Role;
import com.google.cloud.storage.Acl.User;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CSVSerivceUtils {

  @Value("${service.ccscsv.url}")
  private String ccsCsvService;

  @Value("${service.cecsv.url}")
  private String ceCsvService;

  @Value("${service.addresscheckcsv.url}")
  private String addressCheckCsvService;

  @Value("${service.addressfileload.url}")
  private String addressFileLoadService;

  @Value("${service.csvservice.username}")
  private String csvServiceUsername;

  @Value("${service.csvservice.password}")
  private String csvServicePassword;

  @Value("${service.csvservice.gcpBucket.aclocation}")
  String bucketName;

  private static Storage storage = null;

  // [START init]
  static {
    storage = StorageOptions.getDefaultInstance().getService();
  }

  public CSVSerivceUtils() {
  }

  public int enableCCSCsvService() {
    return sendRequest(ccsCsvService);
  }

  public int enableCECsvService() {
    return sendRequest(ceCsvService);
  }

  public int enableAddressCheckCsvService() {
    return sendRequest(addressCheckCsvService);
  }

  public int ingestAddressCheckFile() {
    return sendRequest(addressFileLoadService);
  }

  private int sendRequest(String url) {
    HttpHeaders headers = createBasicAuthHeaders(csvServiceUsername, csvServicePassword);

    headers.setContentType(MediaType.APPLICATION_JSON);

    RestTemplate restTemplate = new RestTemplate();

    HttpEntity<String> get = new HttpEntity<>(null, headers);
    ResponseEntity<Void> response = restTemplate.exchange(url, HttpMethod.GET, get, Void.class);

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

  public void putCSVInBucket(String type, String csv) throws IOException {
    String filename = type + ".csv";
    uploadFile(new ByteArrayInputStream(csv.getBytes()), filename);
    listBlobs("processed/");
  }

  private String uploadFile(InputStream is, String filename) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] readBuf = new byte[4096];
    while (is.available() > 0) {
      int bytesRead = is.read(readBuf);
      os.write(readBuf, 0, bytesRead);
    }

    // Convert ByteArrayOutputStream into byte[]
    BlobInfo blobInfo = storage.create(
        BlobInfo
            .newBuilder(bucketName, filename)
            // Modify access list to allow all users with link to read file
            .setAcl(new ArrayList<>(Arrays.asList(Acl.of(User.ofAllUsers(), Role.READER))))
            .build(),
        os.toByteArray());
    // return the public download link
    return blobInfo.getMediaLink();
  }

  public List<Blob> listBlobs(String directory) {
    var list = new ArrayList<Blob>();
    Page<Blob> blobs = storage.list(
        bucketName, BlobListOption.currentDirectory(), BlobListOption.prefix(directory));
    for (Blob blob : blobs.iterateAll()) {
      list.add(blob);
      log.info(blob.getName());
    }
    return list;
  }
}
