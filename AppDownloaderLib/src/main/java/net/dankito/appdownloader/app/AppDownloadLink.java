package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 18/11/16.
 */

public class AppDownloadLink {

  protected String url;

  protected String fileSize;

  protected String fileHash;

  protected String apkSignature;


  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getFileSize() {
    return fileSize;
  }

  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }

  public String getFileHash() {
    return fileHash;
  }

  public void setFileHash(String fileHash) {
    this.fileHash = fileHash;
  }

  public String getApkSignature() {
    return apkSignature;
  }

  public void setApkSignature(String apkSignature) {
    this.apkSignature = apkSignature;
  }


  @Override
  public String toString() {
    return url;
  }

}
