package net.dankito.appdownloader.app.apkverifier;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 03/12/16.
 */

public class DownloadedApkInfo {

  protected AppInfo app;

  protected AppDownloadInfo downloadSource;

  protected String packageName;

  protected List<String> signatureDigests = new ArrayList<>();

  protected String md5CheckSum;

  protected String sha1CheckSum;

  protected String sha256CheckSum;


  public DownloadedApkInfo(AppInfo app, AppDownloadInfo downloadSource, String packageName) {
    this.app = app;
    this.downloadSource = downloadSource;
    this.packageName = packageName;
  }


  public AppInfo getApp() {
    return app;
  }

  public void setApp(AppInfo app) {
    this.app = app;
  }

  public AppDownloadInfo getDownloadSource() {
    return downloadSource;
  }

  public void setDownloadSource(AppDownloadInfo downloadSource) {
    this.downloadSource = downloadSource;
  }

  public String getPackageName() {
    return packageName;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public List<String> getSignatureDigests() {
    return signatureDigests;
  }

  public void setSignatureDigests(List<String> signatureDigests) {
    this.signatureDigests = signatureDigests;
  }

  public String getMd5CheckSum() {
    return md5CheckSum;
  }

  public void setMd5CheckSum(String md5CheckSum) {
    this.md5CheckSum = md5CheckSum;
  }

  public String getSha1CheckSum() {
    return sha1CheckSum;
  }

  public void setSha1CheckSum(String sha1CheckSum) {
    this.sha1CheckSum = sha1CheckSum;
  }

  public String getSha256CheckSum() {
    return sha256CheckSum;
  }

  public void setSha256CheckSum(String sha256CheckSum) {
    this.sha256CheckSum = sha256CheckSum;
  }


  @Override
  public String toString() {
    return packageName;
  }

}
