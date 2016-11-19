package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IAppVerifier {

  AppPackageVerificationResult verifyDownloadedApk(AppDownloadInfo downloadInfo);

}
