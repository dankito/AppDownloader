package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 19/11/16.
 */

public interface InstalledAppsListener {

  void appInstalled(InstalledAppListenerInfo info);

  void appUpdated(InstalledAppListenerInfo info);

  void appRemoved(InstalledAppListenerInfo info);

}
