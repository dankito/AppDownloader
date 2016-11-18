package net.dankito.appdownloader.app;

import net.dankito.appdownloader.downloader.IAppDownloader;

/**
 * Created by ganymed on 18/11/16.
 */

public class AppDownloadLink {

  protected AppInfo appInfo;

  protected IAppDownloader appDownloader;


  protected String url;

  protected String fileSize;

  protected HashAlgorithm hashAlgorithm;

  protected String fileHashSum;

  protected String apkSignature;


  public AppDownloadLink(AppInfo appInfo, IAppDownloader appDownloader) {
    this.appInfo = appInfo;
    this.appDownloader = appDownloader;
  }


  public AppInfo getAppInfo() {
    return appInfo;
  }

  public IAppDownloader getAppDownloader() {
    return appDownloader;
  }

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

  public HashAlgorithm getHashAlgorithm() {
    return hashAlgorithm;
  }

  public void setHashAlgorithm(HashAlgorithm hashAlgorithm) {
    this.hashAlgorithm = hashAlgorithm;
  }

  public String getFileHashSum() {
    return fileHashSum;
  }

  public void setFileHashSum(String fileHashSum) {
    this.fileHashSum = fileHashSum;
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
