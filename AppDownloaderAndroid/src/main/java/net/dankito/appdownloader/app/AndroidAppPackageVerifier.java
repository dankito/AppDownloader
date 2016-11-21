package net.dankito.appdownloader.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.AppState;
import net.dankito.appdownloader.app.model.AppVersion;
import net.dankito.appdownloader.app.model.HashAlgorithm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/**
 * Created by ganymed on 18/11/16.
 */

public class AndroidAppPackageVerifier implements IAppVerifier {

  private static final Logger log = LoggerFactory.getLogger(AndroidAppPackageVerifier.class);


  protected Context context;


  public AndroidAppPackageVerifier(Context context) {
    this.context = context;
  }


  public AppPackageVerificationResult verifyDownloadedApk(AppDownloadInfo downloadInfo) {
    AppInfo appToInstall = downloadInfo.getAppInfo();
    appToInstall.setState(AppState.VERIFYING_SIGNATURE);

    AppPackageVerificationResult result = new AppPackageVerificationResult(downloadInfo);
    Resources resources = context.getResources();

    PackageManager packageManager = context.getPackageManager();
    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(downloadInfo.getDownloadLocationPath(), PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA);

    boolean appPackageCouldBeVerified = false;

    if(packageInfo != null) {
      result.setCompletelyDownloaded(true);

      appPackageCouldBeVerified = verifyPackageNameIsCorrect(appToInstall, packageInfo, result, resources)
          && verifyVersionIsCorrect(appToInstall, packageInfo, result, resources)
          && verifyFileCheckSumIsCorrect(downloadInfo, result, resources)
          && verifyApkSignatureIsCorrect(downloadInfo, packageManager, result, resources);
    }
    else {
      setErrorMessage(result, resources, R.string.error_message_app_file_not_valid);
    }

    if(appPackageCouldBeVerified == false) {
      appToInstall.setToItsDefaultState();
    }

    return result;
  }

  protected boolean verifyPackageNameIsCorrect(AppInfo appToInstall, PackageInfo packageInfo, AppPackageVerificationResult result, Resources resources) {
    if(isPackageNameCorrect(appToInstall, packageInfo)) {
      result.setPackageNameCorrect(true);
      return true;
    }
    else {
      result.setPackageNameCorrect(false);
      setErrorMessage(result, resources, R.string.error_message_app_package_not_correct);
      return false;
    }
  }

  protected boolean isPackageNameCorrect(AppInfo appToInstall, PackageInfo packageInfo) {
    return appToInstall.getPackageName().equals(packageInfo.packageName);
  }

  protected boolean verifyVersionIsCorrect(AppInfo appToInstall, PackageInfo packageInfo, AppPackageVerificationResult result, Resources resources) {
    if(isVersionCorrect(appToInstall, packageInfo)) {
      result.setVersionCorrect(true);
      return true;
    }
    else {
      result.setVersionCorrect(false);
      setErrorMessage(result, resources, R.string.error_message_app_version_not_correct);
      return false;
    }
  }

  protected boolean isVersionCorrect(AppInfo appToInstall, PackageInfo packageInfo) {
    AppVersion packageVersion = AppVersion.parse(packageInfo.versionName);
    return appToInstall.isVersionSet() == false || packageVersion.equals(appToInstall.getVersion());
  }

  protected boolean verifyFileCheckSumIsCorrect(AppDownloadInfo downloadInfo, AppPackageVerificationResult result, Resources resources) {
    if(isFileCheckSumCorrect(downloadInfo)) {
      result.setFileChecksumCorrect(true);
      return true;
    }
    else {
      result.setFileChecksumCorrect(false);
      setErrorMessage(result, resources, R.string.error_message_app_file_checksum_not_correct);
      return false;
    }
  }

  protected boolean isFileCheckSumCorrect(AppDownloadInfo downloadInfo) {
    File file = new File(downloadInfo.getDownloadLocationPath());
    if(file.exists()) {
      try {
        FileInputStream fileInputStream = new FileInputStream(file);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        byte[] buffer = new byte[(int)file.length()];
        dataInputStream.read(buffer);

        byte[] md5CheckSum = calculateCheckSum(HashAlgorithm.MD5.getAlgorithmName(), buffer);
        byte[] sha1CheckSum = calculateCheckSum(HashAlgorithm.SHA1.getAlgorithmName(), buffer);

        dataInputStream.close();

        return verifyAllCheckSumsEqual(downloadInfo, md5CheckSum, sha1CheckSum);
      } catch(Exception e) {
        log.error("Could not verify file check sum");
      }
    }

    return false;
  }

  protected boolean verifyAllCheckSumsEqual(AppDownloadInfo linkAppDownloadedFrom, byte[] md5CheckSum, byte[] sha1CheckSum) {
    AppInfo downloadedApp = linkAppDownloadedFrom.getAppInfo();
    boolean result = true;
    boolean hasIndependentSourceBeenChecked = false;
    int countChecksumsChecked = 0;

    for(AppDownloadInfo downloadInfo : downloadedApp.getDownloadInfos()) {
      if(downloadInfo.isFileChecksumSet()) {
        if(verifyCheckSumsEqual(downloadInfo.getFileChecksum(), downloadInfo.getFileHashAlgorithm() == HashAlgorithm.MD5 ? md5CheckSum : sha1CheckSum)) {
          if(downloadInfo != linkAppDownloadedFrom) {
            hasIndependentSourceBeenChecked = true;
          }
          countChecksumsChecked++;
        }
        else if(downloadInfo.getAppDownloader().isTrustworthySource()) { // file checksum of absolute trustworthy have to equal
          result = false;
        }
      }
    }

    result &= countChecksumsChecked > 0; // we need at least two independent sources to verify that file has correct check sum
    result &= hasIndependentSourceBeenChecked;

    return result;
  }

  protected boolean verifyApkSignatureIsCorrect(AppDownloadInfo downloadInfo, PackageManager packageManager, AppPackageVerificationResult result, Resources resources) {
    if(verifyApkSignature(downloadInfo, packageManager)) {
      result.setAppSignatureCorrect(true);
      return true;
    }
    else {
      result.setAppSignatureCorrect(false);
      setErrorMessage(result, resources, R.string.error_message_app_signature_invalid);
      return false;
    }
  }

  protected boolean verifyApkSignature(AppDownloadInfo downloadInfo, PackageManager packageManager) {
    String validApkSignature = downloadInfo.getAppInfo().getApkSignature();
    if(validApkSignature == null) { // no apk signature found at from downloader's sites -> there's no way we can verify if app has correct signature
      return false;
    }

    // So, you call that, passing in the path to the APK, along with PackageManager.GET_SIGNATURES.
    // If you get null back, the APK was tampered with and does not have valid digital signature.
    // If you get a PackageInfo back, it will have the "signatures", which you can use for comparison purposes.
    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(downloadInfo.getDownloadLocationPath(), PackageManager.GET_SIGNATURES);
    if(packageInfo == null) {
      return false;
    }

    Signature[] signatures = packageInfo.signatures;
    if(signatures == null || signatures.length == 0) {
      return false;
    }

    for(Signature signature : signatures) {
      final byte[] signatureBytes = signature.toByteArray();

//      String certificateDisplayInfo = getCertificateDisplayInfo(signatureBytes);

      try {
        byte[] digest = calculateCheckSum("SHA", signatureBytes);

        return verifyCheckSumsEqual(validApkSignature, digest);
      } catch(Exception e) {
        log.error("Could not validate Certificate signature", e);
      }
    }

    return false;
  }

  private String getCertificateDisplayInfo(byte[] signatureBytes) {
    InputStream certStream = new ByteArrayInputStream(signatureBytes);

    try {
      CertificateFactory certFactory = CertificateFactory.getInstance("X509");
      X509Certificate x509Cert = (X509Certificate) certFactory.generateCertificate(certStream);

      return x509Cert.toString();
    }
    catch (CertificateException e) {
      log.error("Could not get Certificate Display Info", e);
    }

    return null;
  }

  protected byte[] calculateCheckSum(String algorithmName, byte[] signatureBytes) throws NoSuchAlgorithmException {
    MessageDigest messageDigest = MessageDigest.getInstance(algorithmName);
    messageDigest.update(signatureBytes);

    return messageDigest.digest();
  }

  protected boolean verifyCheckSumsEqual(String hexCheckSum, byte[] checkSumBytes) {
    String hexConvertedCheckSumBytes = bytesToHex(checkSumBytes);

    return hexCheckSum.equals(hexConvertedCheckSumBytes);
  }

  protected String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for(byte b: bytes) {
      sb.append(String.format("%02x", b & 0xff));
    }

    return sb.toString();
  }


  protected void setErrorMessage(AppPackageVerificationResult result, Resources resources, int errorMessageStringId) {
    result.setErrorMessage(resources.getString(errorMessageStringId));
  }

}
