package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.listener.InstalledAppsListener;
import net.dankito.appdownloader.app.model.AppInfo;

import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IInstalledAppsManager {

  List<AppInfo> getAllInstalledApps();

  List<AppInfo> getLaunchableApps();

  AppInfo getAppInstallationInfo(String packageName);

  boolean addInstalledAppsListener(InstalledAppsListener listener);

  boolean removeInstalledAppsListener(InstalledAppsListener listener);

}
