package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.listener.UpdatableAppsListener;

/**
 * Created by ganymed on 19/11/16.
 */
public interface IUpdatableAppsManager {

  void getUpdatableAppsAsync(GetUpdatableAppsCallback callback);

  boolean addUpdatableAppsListener(UpdatableAppsListener listener);

  boolean removeUpdatableAppsListener(UpdatableAppsListener listener);

}
