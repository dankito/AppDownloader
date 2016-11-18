package net.dankito.appdownloader.app;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 18/11/16.
 */

public class AppDetailsCache implements IAppDetailsCache {

  protected Map<String, AppInfo> retrievedAppDetailsCache = new ConcurrentHashMap<>();


  @Override
  public void cacheAppDetails(AppInfo app) {
    retrievedAppDetailsCache.put(getAppKey(app), app);
  }

  @Override
  public boolean setAppDetailsForApp(AppInfo app) {
    AppInfo cachedAppDetails = retrievedAppDetailsCache.get(getAppKey(app));

    if(cachedAppDetails != null) {
      setAppDetailsForApp(cachedAppDetails, app);
      return true;
    }

    return false;
  }

  protected void setAppDetailsForApp(AppInfo cachedAppDetails, AppInfo app) {
    app.setVersion(cachedAppDetails.getVersion());

    app.setRating(cachedAppDetails.getRating());
    app.setCountRatings(cachedAppDetails.getCountRatings());

    app.setCountInstallations(cachedAppDetails.getCountInstallations());
  }

  @Override
  public boolean hasAppDetailsRetrievedForApp(AppInfo app) {
    return retrievedAppDetailsCache.containsKey(getAppKey(app));
  }


  protected String getAppKey(AppInfo app) {
    return app.getPackageName();
  }

}
