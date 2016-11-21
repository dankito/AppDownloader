package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.model.AppDownloadInfo;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IAppVerifier {

  AppPackageVerificationResult verifyDownloadedApk(AppDownloadInfo downloadInfo);

}
