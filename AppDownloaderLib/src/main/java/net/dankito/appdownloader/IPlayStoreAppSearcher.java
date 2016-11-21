package net.dankito.appdownloader;

import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.responses.callbacks.GetAppDetailsCallback;
import net.dankito.appdownloader.responses.callbacks.SearchAppsResponseCallback;

/**
 * Created by ganymed on 19/11/16.
 */
public interface IPlayStoreAppSearcher {

  void searchAsync(String searchTerm, SearchAppsResponseCallback callback);

  void getAppDetailsAsync(AppInfo appInfo, GetAppDetailsCallback callback);

  boolean addRetrievedAppDetailsListener(GetAppDetailsCallback listener);

  boolean removeRetrievedAppDetailsListener(GetAppDetailsCallback listener);

}
