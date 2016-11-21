package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.model.AppInfo;

/**
 * Created by ganymed on 20/11/16.
 */
public interface IAppDownloadAndInstallationService {

  void installApp(AppInfo app);

}
