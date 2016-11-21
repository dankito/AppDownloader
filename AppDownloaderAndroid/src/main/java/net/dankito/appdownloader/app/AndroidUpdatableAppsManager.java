package net.dankito.appdownloader.app;

import android.app.Activity;

import net.dankito.appdownloader.IPlayStoreAppSearcher;
import net.dankito.appdownloader.app.model.AppInfo;
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

  protected List<UpdatableAppsListener> updatableAppsListeners = new CopyOnWriteArrayList<>();


  public AndroidUpdatableAppsManager(Activity activity, IInstalledAppsManager installedAppsManager, IPlayStoreAppSearcher playStoreAppSearcher,
                                     IAppDetailsCache appDetailsCache, IThreadPool threadPool) {
    this.activity = activity;
    this.installedAppsManager = installedAppsManager;
    this.playStoreAppSearcher = playStoreAppSearcher;
    this.appDetailsCache = appDetailsCache;
    this.threadPool = threadPool;

    installedAppsManager.addInstalledAppsListener(installedAppsListener);

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
      setAppDetails(app, countDownLatch);
    }

    try { countDownLatch.await(3, TimeUnit.MINUTES); } catch(Exception ignored) { }

    initializationDone();
  }

  protected void setAppDetails(AppInfo app, CountDownLatch countDownLatch) {
    if(appDetailsCache.setAppDetailsForApp(app) == false) {
      getAppDetailsAsync(app, countDownLatch);
    }
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

    callFoundUpdatableAppListeners(app);
  }

  protected void appUpdated(AppInfo app) {
    updatableApps.remove(app.getPackageName());

    callAppUpdatedListeners(app);
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


  public boolean addUpdatableAppsListener(UpdatableAppsListener listener) {
    return updatableAppsListeners.add(listener);
  }

  public boolean removeUpdatableAppsListener(UpdatableAppsListener listener) {
    return updatableAppsListeners.remove(listener);
  }

  protected void callFoundUpdatableAppListeners(AppInfo updatableApp) {
    for(UpdatableAppsListener listener : updatableAppsListeners) {
      listener.foundUpdatableApp(new UpdatableAppsListenerInfo(updatableApp, new ArrayList<AppInfo>(updatableApps.values())));
    }
  }

  protected void callAppUpdatedListeners(AppInfo updatedApp) {
    for(UpdatableAppsListener listener : updatableAppsListeners) {
      listener.appUpdated(new UpdatableAppsListenerInfo(updatedApp, new ArrayList<AppInfo>(updatableApps.values())));
    }
  }


  protected InstalledAppsListener installedAppsListener = new InstalledAppsListener() {
    @Override
    public void appInstalled(final InstalledAppListenerInfo info) {
      threadPool.runAsync(new Runnable() {
        @Override
        public void run() {
          AndroidUpdatableAppsManager.this.appInstalled(info.getInstalledApp());
        }
      });
    }

    @Override
    public void appUpdated(InstalledAppListenerInfo info) {
      installedAppUpdated(info.getInstalledApp());
    }

    @Override
    public void appRemoved(InstalledAppListenerInfo info) {
      installedAppRemoved(info);
    }
  };

  protected void appInstalled(AppInfo installedApp) {
    if(appDetailsCache.hasAppDetailsRetrievedForApp(installedApp)) {
      appDetailsCache.setAppDetailsForApp(installedApp);
    }
    else {
      CountDownLatch countDownLatch = new CountDownLatch(1);
      setAppDetails(installedApp, countDownLatch);

      try { countDownLatch.await(1, TimeUnit.MINUTES); } catch(Exception ignored) { }
    }

    if(installedApp.isUpdatable()) {
      updatableAppFound(installedApp);
    }
  }

  protected void installedAppUpdated(AppInfo installedApp) {
    if(installedApp.isUpdatable()) {
      if(updatableApps.containsKey(installedApp.getPackageName()) == false) {
        updatableAppFound(installedApp);
      }
    }
    else if(updatableApps.containsKey(installedApp.getPackageName())) {
      appUpdated(installedApp);
    }
  }

  protected void installedAppRemoved(InstalledAppListenerInfo info) {
    AppInfo removedApp = info.getInstalledApp();

    if(updatableApps.containsKey(removedApp.getPackageName()) && removedApp.isUpdatable()) {
      appUpdated(removedApp);
    }
  }

}
