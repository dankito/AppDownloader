package net.dankito.appdownloader.util.app;

import net.dankito.appdownloader.app.AppDownloadInfo;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IAppVerifier {

  AppPackageVerificationResult verifyDownloadedApk(AppDownloadInfo downloadInfo);

}
