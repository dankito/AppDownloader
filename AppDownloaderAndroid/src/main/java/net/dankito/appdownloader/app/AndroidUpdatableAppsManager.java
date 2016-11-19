package net.dankito.appdownloader.app;

import android.app.Activity;

import net.dankito.appdownloader.IPlayStoreAppSearcher;
import net.dankito.appdownloader.responses.GetAppDetailsResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDetailsCallback;
import net.dankito.appdownloader.util.IThreadPool;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by ganymed on 19/11/16.
 */

public class AndroidUpdatableAppsManager implements IUpdatableAppsManager {

  protected Activity activity;

  protected IInstalledAppsManager installedAppsManager;

  protected IPlayStoreAppSearcher playStoreAppSearcher;

  protected IAppDetailsCache appDetailsCache;

  protected IThreadPool threadPool;

  protected Boolean isUpdatableAppsManagerInitialized = false;

  protected List<GetUpdatableAppsCallback> clientsWaitingForFindingUpdatableAppsDone = new CopyOnWriteArrayList<>();

  protected Map<String, AppInfo> updatableApps = new ConcurrentHashMap<>();


  public AndroidUpdatableAppsManager(Activity activity, IInstalledAppsManager installedAppsManager, IPlayStoreAppSearcher playStoreAppSearcher,
                                     IAppDetailsCache appDetailsCache, IThreadPool threadPool) {
    this.activity = activity;
    this.installedAppsManager = installedAppsManager;
    this.playStoreAppSearcher = playStoreAppSearcher;
    this.appDetailsCache = appDetailsCache;
    this.threadPool = threadPool;

    initUpdatableAppsManager();
  }


  protected void initUpdatableAppsManager() {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        findAllUpdatableAppsAsync();
      }
    });
  }

  protected void findAllUpdatableAppsAsync() {
    List<AppInfo> installedApps = installedAppsManager.getLaunchableApps();

    CountDownLatch countDownLatch = new CountDownLatch(installedApps.size());

    for(AppInfo app : installedApps) {
      getAppDetailsAsync(app, countDownLatch);
    }

    try { countDownLatch.await(3, TimeUnit.MINUTES); } catch(Exception ignored) { }

    initializationDone();
  }

  protected void getAppDetailsAsync(final AppInfo app, final CountDownLatch countDownLatch) {
    playStoreAppSearcher.getAppDetailsAsync(app, new GetAppDetailsCallback() {
      @Override
      public void completed(GetAppDetailsResponse response) {
        if(response.isSuccessful()) {
          if(app.isUpdatable()) {
            updatableAppFound(app);
          }
        }

        countDownLatch.countDown();
      }
    });
  }

  protected void updatableAppFound(AppInfo app) {
    updatableApps.put(app.getPackageName(), app);
  }

  protected void initializationDone() {
    this.isUpdatableAppsManagerInitialized = true;

    synchronized(isUpdatableAppsManagerInitialized) {
      List<AppInfo> updatableApps = new ArrayList<>(this.updatableApps.values());

      for(GetUpdatableAppsCallback callback : clientsWaitingForFindingUpdatableAppsDone) {
        callback.completed(updatableApps);
      }

      clientsWaitingForFindingUpdatableAppsDone.clear();
    }
  }


  @Override
  public void getUpdatableAppsAsync(GetUpdatableAppsCallback callback) {
    synchronized(isUpdatableAppsManagerInitialized) {
      if(isUpdatableAppsManagerInitialized) {
        callback.completed(new ArrayList<AppInfo>(updatableApps.values()));
      }
      else {
        clientsWaitingForFindingUpdatableAppsDone.add(callback);
      }
    }
  }

}
