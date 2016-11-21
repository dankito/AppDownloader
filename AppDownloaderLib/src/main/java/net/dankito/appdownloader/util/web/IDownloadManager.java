package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;

/**
 * Created by ganymed on 05/11/16.
 */

public interface IDownloadManager {

  void downloadUrlAsync(AppInfo appInfo, AppDownloadInfo downloadInfo, IDownloadCompletedCallback callback);

}
