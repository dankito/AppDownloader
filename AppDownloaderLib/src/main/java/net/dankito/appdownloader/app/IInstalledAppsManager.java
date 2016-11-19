package net.dankito.appdownloader.app;

import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IInstalledAppsManager {

  List<AppInfo> getInstalledApps();

  AppInfo getAppInstallationInfo(String packageName);

  boolean addInstalledAppsListener(InstalledAppsListener listener);

  boolean removeInstalledAppsListener(InstalledAppsListener listener);

}
