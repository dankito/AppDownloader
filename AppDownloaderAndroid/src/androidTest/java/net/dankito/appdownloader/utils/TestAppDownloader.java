package net.dankito.appdownloader.utils;

import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.downloader.AppDownloaderBase;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.web.IWebClient;

/**
 * Created by ganymed on 04/12/16.
 */

public class TestAppDownloader extends AppDownloaderBase {

  public TestAppDownloader(IWebClient webClient) {
    super(webClient);
  }


  @Override
  protected int getTrustworthinessFactor() {
    return AppDownloaderBase.TRUSTWORTHINESS_ABSOLUTE_TRUSTWORTHY;
  }

  @Override
  public void getAppDownloadLinkAsync(AppInfo appToDownload, GetAppDownloadUrlResponseCallback callback) {

  }

}
