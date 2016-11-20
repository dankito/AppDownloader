package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.util.web.IWebClient;

/**
 * Created by ganymed on 02/11/16.
 */

public class ApkLeecherPlayStoreAppDownloaderTest extends AppDownloaderTestBase {

  @Override
  protected IAppDownloader createAppDownloader(IWebClient webClient) {
    return new ApkLeecherPlayStoreAppDownloader(webClient);
  }

}
