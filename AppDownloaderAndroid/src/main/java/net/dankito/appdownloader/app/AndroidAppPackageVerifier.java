package net.dankito.appdownloader.app;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.content.res.Resources;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.apkverifier.DownloadedApkInfo;
import net.dankito.appdownloader.app.apkverifier.IApkFileVerifier;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileCallback;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileResult;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by ganymed on 18/11/16.
 */

public class AndroidAppPackageVerifier implements IAppVerifier {

  private static final Logger log = LoggerFactory.getLogger(AndroidAppPackageVerifier.class);


  protected Context context;

  protected List<IApkFileVerifier> apkFileVerifiers;


  public AndroidAppPackageVerifier(Context context, List<IApkFileVerifier> apkFileVerifiers) {
    this.context = context;
    this.apkFileVerifiers = apkFileVerifiers;
  }


  public void verifyDownloadedApk(AppDownloadInfo downloadInfo, AppPackageVerificationCallback callback) {
    AppInfo appToInstall = downloadInfo.getAppInfo();
    appToInstall.setState(AppState.VERIFYING_SIGNATURE);

    AppPackageVerificationResult result = new AppPackageVerificationResult(downloadInfo);
    Resources resources = context.getResources();

    PackageManager packageManager = context.getPackageManager();
    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(downloadInfo.getDownloadLocationPath(), PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA);

    if(packageInfo != null) {
      result.setCompletelyDownloaded(true);

      if(verifyPackageNameIsCorrect(appToInstall, packageInfo, result, resources)
          && verifyVersionIsCorrect(appToInstall, packageInfo, result, resources)) {
        verifyFileChecksumAndApkSignatureAsync(downloadInfo, packageManager, packageInfo, result, resources, callback);
      }
      else {
        appToInstall.setToItsDefaultState();
        callback.completed(result);
      }
    }
    else {
      appToInstall.setToItsDefaultState();
      setErrorMessage(result, resources, R.string.error_message_app_file_not_valid);
      callback.completed(result);
    }

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


  protected void verifyFileChecksumAndApkSignatureAsync(AppDownloadInfo downloadInfo, PackageManager packageManager, PackageInfo packageInfo, AppPackageVerificationResult result, Resources resources, AppPackageVerificationCallback callback) {
    DownloadedApkInfo downloadedApkInfo = new DownloadedApkInfo(downloadInfo.getAppInfo(), downloadInfo, packageInfo.packageName);
    if(readFileChecksums(downloadInfo, downloadedApkInfo) && readApkSignatures(downloadInfo, packageManager, downloadedApkInfo)) {
      verifyFileChecksumAndApkSignatureAsync(downloadedApkInfo, result, resources, callback);
    }
  }

  protected boolean readFileChecksums(AppDownloadInfo downloadInfo, DownloadedApkInfo downloadedApkInfo) {
    File file = new File(downloadInfo.getDownloadLocationPath());
    if(file.exists()) {
      try {
        FileInputStream fileInputStream = new FileInputStream(file);
        DataInputStream dataInputStream = new DataInputStream(fileInputStream);

        byte[] buffer = new byte[(int)file.length()];
        dataInputStream.read(buffer);

        byte[] md5CheckSum = calculateCheckSum(HashAlgorithm.MD5.getAlgorithmName(), buffer);
        byte[] sha1CheckSum = calculateCheckSum(HashAlgorithm.SHA1.getAlgorithmName(), buffer);
        byte[] sha256CheckSum = calculateCheckSum(HashAlgorithm.SHA256.getAlgorithmName(), buffer);

        downloadedApkInfo.setMd5CheckSum(bytesToHex(md5CheckSum));
        downloadedApkInfo.setSha1CheckSum(bytesToHex(sha1CheckSum));
        downloadedApkInfo.setSha256CheckSum(bytesToHex(sha256CheckSum));

        dataInputStream.close();

        return true;
      } catch(Exception e) {
        log.error("Could not read file check sums of " + downloadInfo.getAppInfo());
      }
    }

    return false;
  }

  protected boolean readApkSignatures(AppDownloadInfo downloadInfo, PackageManager packageManager, DownloadedApkInfo downloadedApkInfo) {
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

    List<String> signatureDigests = new ArrayList<>();

    for(Signature signature : signatures) {
      try {
        final byte[] signatureBytes = signature.toByteArray();
        byte[] digest = calculateCheckSum("SHA", signatureBytes);

        signatureDigests.add(bytesToHex(digest));
      } catch(Exception e) {
        log.error("Could not read signature digest from " + downloadInfo.getAppInfo(), e);
        return false;
      }
    }

    downloadedApkInfo.setSignatureDigests(signatureDigests);

    return signatureDigests.size() > 0;
  }

  protected void verifyFileChecksumAndApkSignatureAsync(DownloadedApkInfo downloadedApkInfo, AppPackageVerificationResult result, Resources resources, AppPackageVerificationCallback callback) {
    final List<VerifyApkFileResult> results = new ArrayList<>(apkFileVerifiers.size());
    final CountDownLatch countDownLatch = new CountDownLatch(apkFileVerifiers.size());

    for(IApkFileVerifier apkFileVerifier : apkFileVerifiers) {
      apkFileVerifier.verifyApkFile(downloadedApkInfo, new VerifyApkFileCallback() {
        @Override
        public void completed(VerifyApkFileResult verifyApkFileResult) {
          results.add(verifyApkFileResult);
          countDownLatch.countDown();
        }
      });
    }

    try { countDownLatch.await(); } catch(Exception ignored) { }

    evaluateVerifyApkFileResults(downloadedApkInfo, results, result, resources, callback);
  }

  protected void evaluateVerifyApkFileResults(DownloadedApkInfo downloadedApkInfo, List<VerifyApkFileResult> verifyApkFileResults, AppPackageVerificationResult result,
                                              Resources resources, AppPackageVerificationCallback callback) {
    evaluateVerifyFileChecksumResults(verifyApkFileResults, result);
    evaluateVerifyApkSignatureResults(verifyApkFileResults, result);

    if(result.isFileChecksumCorrect() == false) {
      setErrorMessage(result, resources, R.string.error_message_app_file_checksum_not_correct);
    }
    else if(result.isAppSignatureCorrect() == false) {
      setErrorMessage(result, resources, R.string.error_message_app_signature_invalid);
    }

    callback.completed(result);
  }

  protected void evaluateVerifyFileChecksumResults(List<VerifyApkFileResult> verifyApkFileResults, AppPackageVerificationResult result) {
    boolean knowsFileChecksum = false, couldVerifyFileChecksum = true, isFileChecksumFromIndependentSource = false;

    for(VerifyApkFileResult verifyApkFileResult : verifyApkFileResults) {
      if(verifyApkFileResult.knowsFileChecksum()) {
        knowsFileChecksum = true;

        couldVerifyFileChecksum &= verifyApkFileResult.couldVerifyFileChecksum();
        if(verifyApkFileResult.isFileChecksumFromIndependentSource()) {
          isFileChecksumFromIndependentSource = verifyApkFileResult.isFileChecksumFromIndependentSource();
        }
      }
    }

    if(knowsFileChecksum) {
      result.setFileChecksumCorrect(couldVerifyFileChecksum);
      result.setHasFileChecksumBeenCheckedFromIndependentSource(isFileChecksumFromIndependentSource);
    }
  }

  protected void evaluateVerifyApkSignatureResults(List<VerifyApkFileResult> verifyApkFileResults, AppPackageVerificationResult result) {
    boolean knowsApkSignature = false, couldVerifyApkSignature = true;

    for(VerifyApkFileResult verifyApkFileResult : verifyApkFileResults) {
      if(verifyApkFileResult.knowsApkSignature()) {
        knowsApkSignature = true;

        couldVerifyApkSignature &= verifyApkFileResult.couldVerifyApkSignature();
      }
    }

    if(knowsApkSignature) {
      result.setAppSignatureCorrect(couldVerifyApkSignature);
    }
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
