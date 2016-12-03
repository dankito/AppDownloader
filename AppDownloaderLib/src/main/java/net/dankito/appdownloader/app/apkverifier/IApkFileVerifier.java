package net.dankito.appdownloader.app.apkverifier;

/**
 * Created by ganymed on 22/11/16.
 */

public interface IApkFileVerifier {

  void verifyApkFile(DownloadedApkInfo downloadedApkInfo, VerifyApkFileCallback callback);

}
