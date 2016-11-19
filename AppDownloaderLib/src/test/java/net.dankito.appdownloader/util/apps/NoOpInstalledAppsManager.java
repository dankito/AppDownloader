package net.dankito.appdownloader.util.apps;

import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.IInstalledAppsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class NoOpInstalledAppsManager implements IInstalledAppsManager {

  @Override
  public List<AppInfo> getInstalledApps() {
    return new ArrayList<>();
  }

  @Override
  public AppInfo getAppInstallationInfo(String packageName) {
    return null;
  }

}
