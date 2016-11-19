package net.dankito.appdownloader.util.app;

import net.dankito.appdownloader.app.AppDownloadInfo;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IAppVerifier {

  boolean verifyDownloadedApk(AppDownloadInfo downloadInfo);

}
