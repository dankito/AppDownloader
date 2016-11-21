package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.model.AppDownloadInfo;

/**
 * Created by ganymed on 18/11/16.
 */

public interface IAppInstaller {

  void installApp(AppDownloadInfo downloadInfo);

}
