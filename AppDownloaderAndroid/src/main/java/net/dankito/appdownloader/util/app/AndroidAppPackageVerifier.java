package net.dankito.appdownloader.util.app;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.MessageDigest;
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


  public boolean verifyDownloadedApk(AppDownloadLink downloadLink) {
    AppInfo appToInstall = downloadLink.getAppInfo();
    PackageManager packageManager = context.getPackageManager();

    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(downloadLink.getDownloadLocationPath(), PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA);

    if(packageInfo != null) {
      return isPackageNameCorrect(appToInstall, packageInfo) && isVersionCorrect(appToInstall, packageInfo) &&
          verifyApkSignature(downloadLink, packageManager);
    }

    return true;
  }

  protected boolean isPackageNameCorrect(AppInfo appToInstall, PackageInfo packageInfo) {
    return appToInstall.getPackageName().equals(packageInfo.packageName);
  }

  protected boolean isVersionCorrect(AppInfo appToInstall, PackageInfo packageInfo) {
    return packageInfo.versionName.equals(appToInstall.getVersion());
  }

  protected boolean verifyApkSignature(AppDownloadLink downloadLink, PackageManager packageManager) {
    // So, you call that, passing in the path to the APK, along with PackageManager.GET_SIGNATURES.
    // If you get null back, the APK was tampered with and does not have valid digital signature.
    // If you get a PackageInfo back, it will have the "signatures", which you can use for comparison purposes.
    PackageInfo packageInfo = packageManager.getPackageArchiveInfo(downloadLink.getDownloadLocationPath(), PackageManager.GET_SIGNATURES);
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
        MessageDigest messageDigest = MessageDigest.getInstance("SHA");
        messageDigest.update(signatureBytes);
        byte[] digest = messageDigest.digest();
        String digestHex = bytesToHex(digest);

        return digestHex.equals(downloadLink.getAppInfo().getApkSignature());
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

  protected String bytesToHex(byte[] bytes) {
    StringBuilder sb = new StringBuilder(bytes.length * 2);
    for(byte b: bytes) {
      sb.append(String.format("%02x", b & 0xff));
    }

    sb.delete(sb.length() - 2, sb.length() -1);

    return sb.toString();
  }

}
