package net.dankito.appdownloader.util.apps;

import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.downloader.AppDownloaderBase;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;

/**
 * Created by ganymed on 04/12/16.
 */

public class TestAppDownloader extends AppDownloaderBase {

  public TestAppDownloader() {
    super(null);
  }


  @Override
  protected int getTrustworthinessFactor() {
    return AppDownloaderBase.TRUSTWORTHINESS_ABSOLUTE_TRUSTWORTHY;
  }

  @Override
  public void getAppDownloadLinkAsync(AppInfo appToDownload, GetAppDownloadUrlResponseCallback callback) {

  }

}
