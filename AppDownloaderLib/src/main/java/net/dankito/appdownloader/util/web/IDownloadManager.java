package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.responses.AppInfo;

/**
 * Created by ganymed on 05/11/16.
 */

public interface IDownloadManager {

  void downloadUrlAsync(AppInfo appInfo, String url);

}
