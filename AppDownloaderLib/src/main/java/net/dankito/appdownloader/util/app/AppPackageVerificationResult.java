package net.dankito.appdownloader.util.app;

import net.dankito.appdownloader.app.AppDownloadInfo;

/**
 * Created by ganymed on 19/11/16.
 */

public class AppPackageVerificationResult {

  protected AppDownloadInfo downloadInfo;

  protected boolean isCompletelyDownloaded;

  protected boolean isPackageNameCorrect;

  protected boolean isVersionCorrect;

  protected boolean isFileChecksumCorrect;

  protected boolean isAppSignatureCorrect;

  protected String errorMessage;


  public AppPackageVerificationResult(AppDownloadInfo downloadInfo) {
    this.downloadInfo = downloadInfo;
  }


  public boolean wasVerificationSuccessful() {
    return isCompletelyDownloaded() && isPackageNameCorrect() && isVersionCorrect() &&
        isFileChecksumCorrect() && isAppSignatureCorrect();
  }


  public AppDownloadInfo getDownloadInfo() {
    return downloadInfo;
  }

  public boolean isCompletelyDownloaded() {
    return isCompletelyDownloaded;
  }

  public void setCompletelyDownloaded(boolean completelyDownloaded) {
    isCompletelyDownloaded = completelyDownloaded;
  }

  public boolean isPackageNameCorrect() {
    return isPackageNameCorrect;
  }

  public void setPackageNameCorrect(boolean packageNameCorrect) {
    isPackageNameCorrect = packageNameCorrect;
  }

  public boolean isVersionCorrect() {
    return isVersionCorrect;
  }

  public void setVersionCorrect(boolean versionCorrect) {
    isVersionCorrect = versionCorrect;
  }

  public boolean isFileChecksumCorrect() {
    return isFileChecksumCorrect;
  }

  public void setFileChecksumCorrect(boolean fileChecksumCorrect) {
    isFileChecksumCorrect = fileChecksumCorrect;
  }

  public boolean isAppSignatureCorrect() {
    return isAppSignatureCorrect;
  }

  public void setAppSignatureCorrect(boolean appSignatureCorrect) {
    isAppSignatureCorrect = appSignatureCorrect;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }


  @Override
  public String toString() {
    if(wasVerificationSuccessful()) {
      return "Successfully verified";
    }

    return errorMessage;
  }

}
