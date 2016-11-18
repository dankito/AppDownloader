package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.responses.DownloadAppResponse;
import net.dankito.appdownloader.responses.callbacks.DownloadAppCallback;
import net.dankito.appdownloader.util.IThreadPool;
import net.dankito.appdownloader.util.ThreadPool;
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

  protected static final String TEST_APP_PACKAGE_NAME = "com.apkdownloader.adr";

  protected static final String TEST_APP_TITLE = "ApkDownloader";


  protected IAppDownloader underTest;


  @Before
  public void setUp() {
    IWebClient webClient = new OkHttpWebClient();
    IThreadPool threadPool = new ThreadPool();

    underTest = createAppDownloader(webClient, threadPool);
  }

  protected abstract IAppDownloader createAppDownloader(IWebClient webClient, IThreadPool threadPool);


  @Test
  public void downloadAppAsync_DownloadApkDownloader() throws Exception {
    final CountDownLatch countDownLatch = new CountDownLatch(1);
    final List<DownloadAppResponse> responseHolder = new ArrayList<>(1);

    AppInfo appToDownload = new AppInfo(TEST_APP_PACKAGE_NAME);
    appToDownload.setTitle(TEST_APP_TITLE);
    appToDownload.setDeveloper("");

    underTest.downloadAppAsync(appToDownload, new DownloadAppCallback() {
      @Override
      public void completed(DownloadAppResponse response) {
        responseHolder.add(response);

        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(3, TimeUnit.MINUTES); } catch(Exception ignored) { }

    Assert.assertEquals(1, responseHolder.size());

    DownloadAppResponse response = responseHolder.get(0);

    Assert.assertTrue(response.isSuccessful());
    Assert.assertEquals(TEST_APP_PACKAGE_NAME, response.getAppToDownload().getPackageName());

    Assert.assertNotNull(response.getDownloadLocation());
    Assert.assertTrue(response.getDownloadLocation().exists());
    Assert.assertTrue(response.getDownloadLocation().length() > 0);
  }
}
