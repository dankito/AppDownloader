package net.dankito.appdownloader.di;

import net.dankito.appdownloader.PlayStoreAppSearcher;
import net.dankito.appdownloader.downloader.ApkDownloaderPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.EvoziPlayStoreAppDownloader;
import net.dankito.appdownloader.util.IThreadPool;
import net.dankito.appdownloader.util.ThreadPool;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.OkHttpWebClient;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by ganymed on 03/11/16.
 */
@Module
public class AndroidDiContainer {

  @Provides
  @Singleton
  public IThreadPool provideThreadPool() {
    return new ThreadPool();
  }

  @Provides
  @Singleton
  public IWebClient provideWebClient(IThreadPool threadPool) {
    return new OkHttpWebClient();
//    return new ApacheHttpClientWebClient(threadPool);
  }

  @Provides
  @Singleton
  public ApkDownloaderPlayStoreAppDownloader provideApkDownloaderPlayStoreAppDownloader(IThreadPool threadPool, IWebClient webClient) {
    return new ApkDownloaderPlayStoreAppDownloader(webClient, threadPool);
  }

  @Provides
  @Singleton
  public EvoziPlayStoreAppDownloader provideEvoziPlayStoreAppDownloader(IThreadPool threadPool, IWebClient webClient) {
    return new EvoziPlayStoreAppDownloader(webClient, threadPool);
  }

  @Provides
  @Singleton
  public PlayStoreAppSearcher providePlayStoreAppSearcher(IWebClient webClient) {
    return new PlayStoreAppSearcher(webClient);
  }

}
