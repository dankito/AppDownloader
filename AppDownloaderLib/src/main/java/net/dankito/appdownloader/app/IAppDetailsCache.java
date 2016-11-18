package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 18/11/16.
 */
public interface IAppDetailsCache {
  void cacheAppDetails(AppInfo app);

  boolean setAppDetailsForApp(AppInfo app);

  boolean hasAppDetailsRetrievedForApp(AppInfo app);
}
