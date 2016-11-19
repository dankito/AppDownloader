package net.dankito.appdownloader.app;

import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class UpdatableAppsListenerInfo {

  protected AppInfo app;

  protected List<AppInfo> allUpdatableApps;


  public UpdatableAppsListenerInfo(AppInfo app, List<AppInfo> allUpdatableApps) {
    this.app = app;
    this.allUpdatableApps = allUpdatableApps;
  }


  public AppInfo getApp() {
    return app;
  }

  public List<AppInfo> getAllUpdatableApps() {
    return allUpdatableApps;
  }


  @Override
  public String toString() {
    return "" + app;
  }

}
