package net.dankito.appdownloader.util.app;

import net.dankito.appdownloader.app.AppInfo;

import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IInstalledAppsManager {

  List<AppInfo> getInstalledApps();

  AppInfo getAppInstallationInfo(String packageName);

}
