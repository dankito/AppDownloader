package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.model.AppInfo;

import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class InstalledAppListenerInfo {

  protected AppInfo installedApp;

  protected boolean isLaunchable;

  protected List<AppInfo> allInstalledApps;

  protected List<AppInfo> launchableApps;


  public InstalledAppListenerInfo(AppInfo installedApp, boolean isLaunchable, List<AppInfo> allInstalledApps, List<AppInfo> launchableApps) {
    this.installedApp = installedApp;
    this.isLaunchable = isLaunchable;
    this.allInstalledApps = allInstalledApps;
    this.launchableApps = launchableApps;
  }


  public AppInfo getInstalledApp() {
    return installedApp;
  }

  public boolean isLaunchable() {
    return isLaunchable;
  }

  public List<AppInfo> getAllInstalledApps() {
    return allInstalledApps;
  }

  public List<AppInfo> getLaunchableApps() {
    return launchableApps;
  }


  @Override
  public String toString() {
    return "" + installedApp;
  }

}
