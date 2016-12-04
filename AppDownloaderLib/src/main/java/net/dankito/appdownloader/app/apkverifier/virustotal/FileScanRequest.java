package net.dankito.appdownloader.app.apkverifier.virustotal;

import net.dankito.appdownloader.util.StringUtils;

/**
 * Created by ganymed on 04/12/16.
 */

public class FileScanRequest {

  protected String filePath;

  protected String sha256Checksum;

  protected String fileMimeType;

  protected String scanId;


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

  public void setSha256Checksum(String sha256Checksum) {
    this.sha256Checksum = sha256Checksum;
  }

  public String getFileMimeType() {
    return fileMimeType;
  }

  public boolean isScanIdSet() {
    return StringUtils.isNotNullOrEmpty(getScanId());
  }

  public String getScanId() {
    return scanId;
  }

  public void setScanId(String scanId) {
    this.scanId = scanId;
  }


  @Override
  public String toString() {
    return getFilePath();
  }

}
