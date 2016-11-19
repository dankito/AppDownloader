package net.dankito.appdownloader.app;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;

import net.dankito.appdownloader.R;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
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


  protected Activity context;


  public AndroidAppPackageVerifier(Activity context) {
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
    return packageInfo.versionName.equals(appToInstall.getVersionString());
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

        ByteBuffer buffer = ByteBuffer.allocate((int)file.length());
        dataInputStream.read(buffer.array());

        byte[] fileCheckSum = calculateCheckSum(downloadInfo.getHashAlgorithm().getAlgorithmName(), buffer);

        dataInputStream.close();

        return verifyCheckSumsEqual(downloadInfo.getFileHashSum(), fileCheckSum);
      } catch(Exception e) {
        log.error("Could not verify file check sum");
      }
    }

    return false;
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

        return verifyCheckSumsEqual(downloadInfo.getAppInfo().getApkSignature(), digest);
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

  protected byte[] calculateCheckSum(String algorithmName, ByteBuffer signatureBytes) throws NoSuchAlgorithmException {
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
