package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 19/11/16.
 */
public interface IUpdatableAppsManager {

  void getUpdatableAppsAsync(GetUpdatableAppsCallback callback);

  boolean addUpdatableAppsListener(UpdatableAppsListener listener);

  boolean removeUpdatableAppsListener(UpdatableAppsListener listener);

}
