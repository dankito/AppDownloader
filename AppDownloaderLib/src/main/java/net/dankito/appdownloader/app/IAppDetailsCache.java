package net.dankito.appdownloader.app;

import net.dankito.appdownloader.app.model.AppInfo;

/**
 * Created by ganymed on 18/11/16.
 */
public interface IAppDetailsCache {
  void cacheAppDetails(AppInfo app);

  boolean setAppDetailsForApp(AppInfo app);

  boolean hasAppDetailsRetrievedForApp(AppInfo app);
}
