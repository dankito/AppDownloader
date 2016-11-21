package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;

/**
 * Created by ganymed on 04/11/16.
 */

public interface IAppDownloader {

  boolean isTrustworthySource();

  void getAppDownloadLinkAsync(AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback);

}
