package net.dankito.appdownloader.di;

import android.app.Activity;

import net.dankito.appdownloader.PlayStoreAppSearcher;
import net.dankito.appdownloader.app.AppDetailsCache;
import net.dankito.appdownloader.app.IAppDetailsCache;
import net.dankito.appdownloader.downloader.ApkDownloaderPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.EvoziPlayStoreAppDownloader;
import net.dankito.appdownloader.util.AndroidAppInstaller;
import net.dankito.appdownloader.util.AndroidOnUiThreadRunner;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.util.IThreadPool;
import net.dankito.appdownloader.util.ThreadPool;
import net.dankito.appdownloader.util.app.IAppInstaller;
import net.dankito.appdownloader.util.web.AndroidDownloadManager;
import net.dankito.appdownloader.util.web.IDownloadManager;
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

  protected final Activity activity;


  public AndroidDiContainer (Activity activity) {
    this.activity = activity;
  }


  @Provides //scope is not necessary for parameters stored within the module
  public Activity getActivity() {
    return activity;
  }


  @Provides
  @Singleton
  public IThreadPool provideThreadPool() {
    return new ThreadPool();
  }

  @Provides
  @Singleton
  public IOnUiThreadRunner provideOnUiThreadRunner() {
    return new AndroidOnUiThreadRunner(getActivity());
  }


  @Provides
  @Singleton
  public IWebClient provideWebClient() {
    return new OkHttpWebClient();
  }

  @Provides
  @Singleton
  public IDownloadManager provideDownloadManager() {
    return new AndroidDownloadManager(getActivity());
  }

  @Provides
  @Singleton
  public IAppDetailsCache provideAppDetailsCache() {
    return new AppDetailsCache();
  }


  @Provides
  @Singleton
  public IAppInstaller provideAppInstaller() {
    return new AndroidAppInstaller(getActivity());
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
  public PlayStoreAppSearcher providePlayStoreAppSearcher(IWebClient webClient, IAppDetailsCache appDetailsCache) {
    return new PlayStoreAppSearcher(webClient, appDetailsCache);
  }

}
