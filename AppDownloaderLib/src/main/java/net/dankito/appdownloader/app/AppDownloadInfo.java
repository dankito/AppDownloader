package net.dankito.appdownloader.app;

import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.util.StringUtils;

/**
 * Created by ganymed on 18/11/16.
 */

public class AppDownloadInfo {

  protected AppInfo appInfo;

  protected IAppDownloader appDownloader;


  protected String url;

  protected boolean hasDownloadLink;

  protected String fileSize;

  protected HashAlgorithm hashAlgorithm;

  protected String fileHashSum;

  protected String apkSignature;


  protected String downloadLocationUri = null;

  protected String downloadLocationPath = null;


  public AppDownloadInfo(AppInfo appInfo, IAppDownloader appDownloader) {
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
    this.hasDownloadLink = StringUtils.isNotNullOrEmpty(url); // TODO: check if it's a valid url
  }

  public boolean isHasDownloadLink() {
    return hasDownloadLink;
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

  public boolean isApkSignatureSet() {
    return StringUtils.isNotNullOrEmpty(getApkSignature());
  }

  public String getApkSignature() {
    return apkSignature;
  }

  public void setApkSignature(String apkSignature) {
    this.apkSignature = apkSignature;
  }


  public boolean isDownloaded() {
    return StringUtils.isNotNullOrEmpty(getDownloadLocationPath());
  }

  public String getDownloadLocationUri() {
    return downloadLocationUri;
  }

  public void setDownloadLocationUri(String downloadLocationUri) {
    this.downloadLocationUri = downloadLocationUri;
  }

  public String getDownloadLocationPath() {
    return downloadLocationPath;
  }

  public void setDownloadLocationPath(String downloadLocationPath) {
    this.downloadLocationPath = downloadLocationPath;
  }


  @Override
  public String toString() {
    return url;
  }

}
