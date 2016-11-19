package net.dankito.appdownloader.app;

import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public interface GetUpdatableAppsCallback {

  void completed(List<AppInfo> updatableApps);

}
