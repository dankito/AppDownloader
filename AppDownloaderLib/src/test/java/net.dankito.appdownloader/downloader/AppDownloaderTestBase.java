package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppDownloadInfo;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.OkHttpWebClient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 02/11/16.
 */

public abstract class AppDownloaderTestBase {

  protected static final String TEST_APP_PACKAGE_NAME = "com.whatsapp";

  protected static final String TEST_APP_TITLE = "WhatsApp Messenger";


  protected IAppDownloader underTest;


  @Before
  public void setUp() {
    IWebClient webClient = new OkHttpWebClient();

    underTest = createAppDownloader(webClient);
  }

  protected abstract IAppDownloader createAppDownloader(IWebClient webClient);


  @Test
  public void downloadAppAsync_DownloadApkDownloader() throws Exception {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<GetAppDownloadUrlResponse> responseHolder = new ArrayList<>(1);

    AppInfo appToDownload = new AppInfo(TEST_APP_PACKAGE_NAME);
    appToDownload.setTitle(TEST_APP_TITLE);
    appToDownload.setDeveloper("");

    underTest.getAppDownloadLinkAsync(appToDownload, new GetAppDownloadUrlResponseCallback() {
      @Override
      public void completed(GetAppDownloadUrlResponse response) {
        responseHolder.add(response);

        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(3, TimeUnit.MINUTES); } catch(Exception ignored) { }

    Assert.assertEquals(1, responseHolder.size());

    GetAppDownloadUrlResponse response = responseHolder.get(0);

    Assert.assertTrue(response.isSuccessful());

    AppInfo appToDownloadFromResponse = response.getAppToDownload();
    Assert.assertEquals(TEST_APP_PACKAGE_NAME, appToDownloadFromResponse.getPackageName());

    List<AppDownloadInfo> downloadInfos = appToDownloadFromResponse.getDownloadInfos();
    Assert.assertEquals(1, downloadInfos.size());

    AppDownloadInfo downloadInfo = downloadInfos.get(0);
    Assert.assertNotNull(downloadInfo.getAppDownloader());
    Assert.assertNotNull(downloadInfo.getAppInfo());
    Assert.assertNotNull(downloadInfo.getUrl());
    Assert.assertNotNull(downloadInfo.getFileSize());
    Assert.assertNotNull(downloadInfo.getFileChecksum());
    Assert.assertNotNull(downloadInfo.getFileHashAlgorithm());
  }
}
