package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.responses.AppSearchResult;
import net.dankito.appdownloader.responses.callbacks.DownloadAppCallback;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;

/**
 * Created by ganymed on 04/11/16.
 */

public interface IAppDownloader {

  void downloadAppAsync(final AppSearchResult appToDownload, final DownloadAppCallback callback);

  void getAppDownloadLinkAsync(AppSearchResult appToDownload, final GetAppDownloadUrlResponseCallback callback);

}
