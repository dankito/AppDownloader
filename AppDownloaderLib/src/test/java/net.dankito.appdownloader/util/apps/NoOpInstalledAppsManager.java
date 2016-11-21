package net.dankito.appdownloader.util.apps;

import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.IInstalledAppsManager;
import net.dankito.appdownloader.app.InstalledAppsListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class NoOpInstalledAppsManager implements IInstalledAppsManager {

  @Override
  public List<AppInfo> getAllInstalledApps() {
    return new ArrayList<>();
  }

  @Override
  public List<AppInfo> getLaunchableApps() {
    return null;
  }

  @Override
  public AppInfo getAppInstallationInfo(String packageName) {
    return null;
  }

  @Override
  public boolean addInstalledAppsListener(InstalledAppsListener listener) {
    return false;
  }

  @Override
  public boolean removeInstalledAppsListener(InstalledAppsListener listener) {
    return false;
  }

}
