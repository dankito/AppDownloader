package net.dankito.appdownloader.app.model;

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

  protected HashAlgorithm fileHashAlgorithm;

  protected String fileChecksum;

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

  public boolean hasDownloadLink() {
    return hasDownloadLink;
  }

  public String getFileSize() {
    return fileSize;
  }

  public void setFileSize(String fileSize) {
    this.fileSize = fileSize;
  }

  public HashAlgorithm getFileHashAlgorithm() {
    return fileHashAlgorithm;
  }

  public void setFileHashAlgorithm(HashAlgorithm fileHashAlgorithm) {
    this.fileHashAlgorithm = fileHashAlgorithm;
  }

  public boolean isFileChecksumSet() {
    return StringUtils.isNotNullOrEmpty(getFileChecksum()) && getFileHashAlgorithm() != null;
  }

  public String getFileChecksum() {
    return fileChecksum;
  }

  public void setFileChecksum(String fileChecksum) {
    this.fileChecksum = fileChecksum;
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
