package net.dankito.appdownloader;

import net.dankito.appdownloader.responses.AppSearchResult;
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
    final List<AppSearchResult> appSearchResults = new ArrayList<>();
    final CountDownLatch countDownLatch = new CountDownLatch(1);

    underTest.searchAsync("ausgaben manager", new SearchAppsResponseCallback() {
      @Override
      public void completed(SearchAppsResponse response) {
        appSearchResults.addAll(response.getAppSearchResults());
        countDownLatch.countDown();
      }
    });

    try { countDownLatch.await(5, TimeUnit.SECONDS); } catch(Exception ignored) { }

    Assert.assertTrue(appSearchResults.size() > 0);

    for(AppSearchResult appSearchResult : appSearchResults) {
      Assert.assertTrue(appSearchResult.areNecessaryInformationSet());

      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appSearchResult.getAppUrl()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appSearchResult.getPackageName()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appSearchResult.getTitle()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appSearchResult.getDeveloper()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appSearchResult.getSmallCoverImageUrl()));
      Assert.assertTrue(StringUtils.isNotNullOrEmpty(appSearchResult.getLargeCoverImageUrl()));
    }
  }
}
