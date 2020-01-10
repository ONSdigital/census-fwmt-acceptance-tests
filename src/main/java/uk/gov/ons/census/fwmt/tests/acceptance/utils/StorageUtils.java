package uk.gov.ons.census.fwmt.tests.acceptance.utils;

import com.google.api.gax.paging.Page;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class StorageUtils {

  private static Storage storage;

  static {
    storage = StorageOptions.getDefaultInstance().getService();
  }

  public void createFile(String type, String csv, String location) throws IOException {
    String filename = type + ".csv";
    uploadFile(new ByteArrayInputStream(csv.getBytes()), filename, location);
  }

  public void deleteFiles(String directory) {
    if(directory.startsWith("gs:")) {
      String bucket = directory.substring(directory.indexOf(":")+1);
      bucket = bucket.trim().replaceAll("/","");
      List<Blob> blobsForDeletion = listBlobs(bucket);
      for (Blob blob : blobsForDeletion) {
        blob.delete();
      }
    } else if (directory.startsWith("file:")) {
      String[] filesForDeletion = listDirectory(directory);
      for (String fileName : filesForDeletion) {
        File fileForDeletion = new File(fileName);
        fileForDeletion.delete();
      }
    }
  }

  private void uploadFile(InputStream is, String filename, String location) throws IOException {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    byte[] readBuf = new byte[4096];
    while (is.available() > 0) {
      int bytesRead = is.read(readBuf);
      os.write(readBuf, 0, bytesRead);
    }

    if (location.startsWith("gs:")) {
      String bucket = location.substring(location.indexOf(":")+1);
      bucket = bucket.trim().replaceAll("/","");
     storage.create(
          BlobInfo
              .newBuilder(bucket, filename)
              .setAcl(new ArrayList<>(Collections.singletonList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
              .build(),
          os.toByteArray());

    } else if (location.startsWith("file:")) {
      try(OutputStream outputStream = new FileOutputStream(filename)) {
        os.writeTo(outputStream);
      }
    }
  }

  private List<Blob> listBlobs(String directory) {
    var list = new ArrayList<Blob>();
    Page<Blob> blobs = storage.list(
        directory, Storage.BlobListOption.currentDirectory(), Storage.BlobListOption.prefix(directory));
    for (Blob blob : blobs.iterateAll()) {
      list.add(blob);
      log.info(blob.getName());
    }
    return list;
  }

  private String[] listDirectory(String directory) {
    String[] list;
    File f = new File(directory);
    list = f.list();

    return list;
  }
}
