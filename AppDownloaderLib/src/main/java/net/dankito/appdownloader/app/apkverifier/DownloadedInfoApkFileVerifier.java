package net.dankito.appdownloader.app.apkverifier;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.HashAlgorithm;
import net.dankito.appdownloader.downloader.FDroidAppDownloader;
import net.dankito.appdownloader.util.StringUtils;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 03/12/16.
 */

public class DownloadedInfoApkFileVerifier implements IApkFileVerifier {

  protected static final String FDROID_APK_SIGNATURE = "b4e515d5cda1958213f6d73383ee9b14987985fc";


  @Override
  public void verifyApkFile(DownloadedApkInfo downloadedApkInfo, VerifyApkFileCallback callback) {
    VerifyApkFileResult verifyApkFileResult = verifyFileCheckSumIsCorrect(downloadedApkInfo);

    verifyApkSignatureIsCorrect(downloadedApkInfo, verifyApkFileResult);

    callback.completed(verifyApkFileResult);
  }


  protected VerifyApkFileResult verifyFileCheckSumIsCorrect(DownloadedApkInfo downloadedApkInfo) {
    AppInfo downloadedApp = downloadedApkInfo.getApp();
    AppDownloadInfo linkAppDownloadedFrom = downloadedApkInfo.getDownloadSource();

    AtomicBoolean checksumHasBeenVerifiedFromIndependentSource = new AtomicBoolean(false);
    boolean result = true;
    int countChecksumsChecked = 0;

    for(AppDownloadInfo downloadInfo : downloadedApp.getDownloadInfos()) {
      if(downloadInfo.isFileChecksumSet()) {
        String fileChecksum = getFileChecksumForHashAlgorithm(downloadedApkInfo, downloadInfo);

        if(downloadInfo.getFileChecksum().equals(fileChecksum)) {
          if(downloadInfo != linkAppDownloadedFrom) {
            checksumHasBeenVerifiedFromIndependentSource.set(true);
          }
          countChecksumsChecked++;
        }
        else if(downloadInfo.getAppDownloader().isTrustworthySource()) { // file checksum of absolute trustworthy have to equal
          result = false;
        }
      }
    }

    result &= countChecksumsChecked > 0; // we need at least two independent sources to verify that file has correct check sum

    return new VerifyApkFileResult(downloadedApkInfo, countChecksumsChecked > 0, result, checksumHasBeenVerifiedFromIndependentSource.get());
  }

  protected String getFileChecksumForHashAlgorithm(DownloadedApkInfo downloadedApkInfo, AppDownloadInfo downloadInfo) {
    String fileChecksum = downloadedApkInfo.getMd5CheckSum();

    if(downloadInfo.getFileHashAlgorithm() == HashAlgorithm.SHA1) {
      fileChecksum = downloadedApkInfo.getSha1CheckSum();
    }
    else if(downloadInfo.getFileHashAlgorithm() == HashAlgorithm.SHA256) {
      fileChecksum = downloadedApkInfo.getSha256CheckSum();
    }

    return fileChecksum;
  }

  protected void verifyApkSignatureIsCorrect(DownloadedApkInfo downloadedApkInfo, VerifyApkFileResult verifyApkFileResult) {
    String apkSignature = downloadedApkInfo.getApp().getApkSignature();
    if(StringUtils.isNotNullOrEmpty(apkSignature)) {
      verifyApkFileResult.setKnowsApkSignature(true);

      boolean signatureCheckResult = downloadedApkInfo.getSignatureDigests().size() > 0;

      for(String signatureDigest : downloadedApkInfo.getSignatureDigests()) {
        if(apkSignature.equals(signatureDigest)) {
          signatureCheckResult &= true;
        }
        else if(FDROID_APK_SIGNATURE.equals(signatureDigest) && downloadedApkInfo.getDownloadSource().getAppDownloader() instanceof FDroidAppDownloader) {
          signatureCheckResult &= true;
        }
        else {
          signatureCheckResult = false;
        }
      }

      verifyApkFileResult.setCouldVerifyApkSignature(signatureCheckResult);
    }
  }

}
