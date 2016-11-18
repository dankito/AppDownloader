package net.dankito.appdownloader;

import net.dankito.appdownloader.responses.AppInfo;
import net.dankito.appdownloader.responses.SearchAppsResponse;
import net.dankito.appdownloader.responses.callbacks.SearchAppsResponseCallback;
import net.dankito.appdownloader.util.StringUtils;
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

public class PlayStoreAppSearcherTest {

  protected PlayStoreAppSearcher underTest;


  @Before
  public void setUp() {
    IWebClient webClient = new OkHttpWebClient();
    underTest = new PlayStoreAppSearcher(webClient);
  }


  @Test
  public void searchAsync() {
    final List<AppInfo> appInfos = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.searchAsync("ausgaben manager", new SearchAppsResponseCallback() {
      @Override
      public void completed(SearchAppsResponse response) {
        appInfos.addAll(response.getAppInfos());
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(appInfos.size() > 0);

    for(AppInfo appInfo : appInfos) {
      Assert.assertTrue(appInfo.areNecessaryInformationSet());

      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getAppUrl()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getPackageName()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getTitle()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getDeveloper()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getSmallCoverImageUrl()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appInfo.getLargeCoverImageUrl()));
    }
  }
}
