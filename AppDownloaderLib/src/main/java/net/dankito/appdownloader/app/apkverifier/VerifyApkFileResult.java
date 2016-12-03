package net.dankito.appdownloader.app.apkverifier;

import net.dankito.appdownloader.responses.ResponseBase;

/**
 * Created by ganymed on 03/12/16.
 */

public class VerifyApkFileResult extends ResponseBase {

  protected DownloadedApkInfo downloadedApkInfo;

  protected boolean knowsFileChecksum;

  protected boolean couldVerifyFileChecksum;

  protected boolean isFileChecksumFromIndependentSource;

  protected boolean knowsApkSignature;

  protected boolean couldVerifyApkSignature;


  public VerifyApkFileResult(DownloadedApkInfo downloadedApkInfo, String error) {
    super(error);
    this.downloadedApkInfo = downloadedApkInfo;
  }

  public VerifyApkFileResult(DownloadedApkInfo downloadedApkInfo, boolean knowsFileChecksum, boolean couldVerifyFileChecksum, boolean isFileChecksumFromIndependentSource) {
    super(true);
    this.downloadedApkInfo = downloadedApkInfo;
    this.knowsFileChecksum = knowsFileChecksum;
    this.couldVerifyFileChecksum = couldVerifyFileChecksum;
    this.isFileChecksumFromIndependentSource = isFileChecksumFromIndependentSource;
  }


  public DownloadedApkInfo getDownloadedApkInfo() {
    return downloadedApkInfo;
  }

  public boolean knowsFileChecksum() {
    return knowsFileChecksum;
  }

  public void setKnowsFileChecksum(boolean knowsFileChecksum) {
    this.knowsFileChecksum = knowsFileChecksum;
  }

  public boolean couldVerifyFileChecksum() {
    return couldVerifyFileChecksum;
  }

  public void setCouldVerifyFileChecksum(boolean couldVerifyFileChecksum) {
    this.couldVerifyFileChecksum = couldVerifyFileChecksum;
  }

  public boolean isFileChecksumFromIndependentSource() {
    return isFileChecksumFromIndependentSource;
  }

  public void setFileChecksumFromIndependentSource(boolean fileChecksumFromIndependentSource) {
    this.isFileChecksumFromIndependentSource = fileChecksumFromIndependentSource;
  }

  public boolean knowsApkSignature() {
    return knowsApkSignature;
  }

  public void setKnowsApkSignature(boolean knowsApkSignature) {
    this.knowsApkSignature = knowsApkSignature;
  }

  public boolean couldVerifyApkSignature() {
    return couldVerifyApkSignature;
  }

  public void setCouldVerifyApkSignature(boolean couldVerifyApkSignature) {
    this.couldVerifyApkSignature = couldVerifyApkSignature;
  }


  @Override
  public String toString() {
    return "" + downloadedApkInfo;
  }

}
