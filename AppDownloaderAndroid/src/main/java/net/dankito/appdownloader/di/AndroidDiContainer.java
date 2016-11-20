package net.dankito.appdownloader.di;

import android.app.Activity;

import net.dankito.appdownloader.IPlayStoreAppSearcher;
import net.dankito.appdownloader.PlayStoreAppSearcher;
import net.dankito.appdownloader.app.AndroidAppDownloadAndInstallationService;
import net.dankito.appdownloader.app.AndroidAppInstaller;
import net.dankito.appdownloader.app.AndroidAppPackageVerifier;
import net.dankito.appdownloader.app.AndroidInstalledAppsManager;
import net.dankito.appdownloader.app.AndroidUpdatableAppsManager;
import net.dankito.appdownloader.app.AppDetailsCache;
import net.dankito.appdownloader.app.IAppDetailsCache;
import net.dankito.appdownloader.app.IAppDownloadAndInstallationService;
import net.dankito.appdownloader.app.IAppInstaller;
import net.dankito.appdownloader.app.IAppVerifier;
import net.dankito.appdownloader.app.IInstalledAppsManager;
import net.dankito.appdownloader.app.IUpdatableAppsManager;
import net.dankito.appdownloader.downloader.ApkDownloaderPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.ApkLeecherPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.ApkMirrorPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.EvoziPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.util.AndroidOnUiThreadRunner;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.util.IThreadPool;
import net.dankito.appdownloader.util.ThreadPool;
import net.dankito.appdownloader.util.web.AndroidDownloadManager;
import net.dankito.appdownloader.util.web.IDownloadManager;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.OkHttpWebClient;

import java.util.ArrayList;
import java.util.List;

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
  public IInstalledAppsManager provideInstalledAppsManager() {
    return new AndroidInstalledAppsManager(getActivity());
  }

  @Provides
  @Singleton
  public IUpdatableAppsManager provideUpdatableAppsManager(IInstalledAppsManager installedAppsManager, IPlayStoreAppSearcher playStoreAppSearcher,
                                                           IAppDetailsCache appDetailsCache, IThreadPool threadPool) {
    return new AndroidUpdatableAppsManager(getActivity(), installedAppsManager, playStoreAppSearcher, appDetailsCache, threadPool);
  }

  @Provides
  @Singleton
  public IAppVerifier provideAppVerifier() {
    return new AndroidAppPackageVerifier(getActivity());
  }

  @Provides
  @Singleton
  public IAppInstaller provideAppInstaller() {
    return new AndroidAppInstaller(getActivity());
  }

  @Provides
  @Singleton
  public IAppDownloadAndInstallationService provideAppDownloadAndInstallationService(List<IAppDownloader> appDownloaders, IDownloadManager downloadManager,
                                                                                     IAppVerifier appVerifier, IAppInstaller appInstaller) {
    return new AndroidAppDownloadAndInstallationService(getActivity(), appDownloaders, downloadManager, appVerifier, appInstaller);
  }


  @Provides
  @Singleton
  public ApkMirrorPlayStoreAppDownloader provideApkMirrorPlayStoreAppDownloader(IWebClient webClient) {
    return new ApkMirrorPlayStoreAppDownloader(webClient);
  }

  @Provides
  @Singleton
  public ApkDownloaderPlayStoreAppDownloader provideApkDownloaderPlayStoreAppDownloader(IWebClient webClient) {
    return new ApkDownloaderPlayStoreAppDownloader(webClient);
  }

  @Provides
  @Singleton
  public ApkLeecherPlayStoreAppDownloader provideApkLeecherPlayStoreAppDownloader(IWebClient webClient) {
    return new ApkLeecherPlayStoreAppDownloader(webClient);
  }

  @Provides
  @Singleton
  public EvoziPlayStoreAppDownloader provideEvoziPlayStoreAppDownloader(IWebClient webClient) {
    return new EvoziPlayStoreAppDownloader(webClient);
  }

  @Provides
  @Singleton
  public List<IAppDownloader> provideAppDownloaders(ApkMirrorPlayStoreAppDownloader apkMirrorDownloader, ApkDownloaderPlayStoreAppDownloader apkDownloaderDownloader,
                                                    ApkLeecherPlayStoreAppDownloader apkLeecherDownloader, EvoziPlayStoreAppDownloader evoziDownloader) {
    List<IAppDownloader> appDownloaders = new ArrayList<>();

    appDownloaders.add(apkMirrorDownloader);
    appDownloaders.add(apkDownloaderDownloader);
    appDownloaders.add(apkLeecherDownloader);
    appDownloaders.add(evoziDownloader);

    return appDownloaders;
  }

  @Provides
  @Singleton
  public IPlayStoreAppSearcher providePlayStoreAppSearcher(IWebClient webClient, IInstalledAppsManager installedAppsManager, IAppDetailsCache appDetailsCache) {
    return new PlayStoreAppSearcher(webClient, installedAppsManager, appDetailsCache);
  }

}
