package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;

/**
 * Created by ganymed on 04/11/16.
 */

public interface IAppDownloader {

  void getAppDownloadLinkAsync(AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback);

}
