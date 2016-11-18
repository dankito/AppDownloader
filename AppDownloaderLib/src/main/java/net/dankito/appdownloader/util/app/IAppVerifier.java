package net.dankito.appdownloader.util.app;

import net.dankito.appdownloader.app.AppDownloadLink;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IAppVerifier {

  boolean verifyDownloadedApk(AppDownloadLink downloadLink);

}
