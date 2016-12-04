package net.dankito.appdownloader.app.apkverifier.virustotal;

/**
 * Created by ganymed on 04/12/16.
 */

public class FileScanRequest {

  protected String filePath;

  protected String sha256Checksum;

  protected String fileMimeType;


  public FileScanRequest(String filePath, String sha256Checksum, String fileMimeType) {
    this.filePath = filePath;
    this.sha256Checksum = sha256Checksum;
    this.fileMimeType = fileMimeType;
  }


  public String getFilePath() {
    return filePath;
  }

  public String getSha256Checksum() {
    return sha256Checksum;
  }

  public String getFileMimeType() {
    return fileMimeType;
  }


  @Override
  public String toString() {
    return getFilePath();
  }

}
