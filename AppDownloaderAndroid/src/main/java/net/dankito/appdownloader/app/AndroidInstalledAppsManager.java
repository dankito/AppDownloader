package net.dankito.appdownloader.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 19/11/16.
 */

public class AndroidInstalledAppsManager implements IInstalledAppsManager {

  private static final Logger log = LoggerFactory.getLogger(AndroidInstalledAppsManager.class);


  protected Activity activity;

  protected boolean installedAppsRetrieved = false;

  protected Map<String, AppInfo> allInstalledApps = new ConcurrentHashMap<>();

  protected Map<String, AppInfo> launchableApps = new ConcurrentHashMap<>();

  protected List<InstalledAppsListener> installedAppsListeners = new CopyOnWriteArrayList<>();


  public AndroidInstalledAppsManager(Activity activity) {
    this.activity = activity;

    registerBroadcastReceivers(activity);

    initAppsManager();
  }


  protected void registerBroadcastReceivers(Activity activity) {
    IntentFilter packageAddedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    packageAddedIntentFilter.addDataScheme("package"); // this seems to be absolutely necessary for PACKAGE_ADDED, see https://stackoverflow.com/questions/11246326/how-to-receiving-broadcast-when-application-installed-or-removed
    activity.registerReceiver(broadcastReceiver, packageAddedIntentFilter);

    IntentFilter packageRemovedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_REMOVED);
    packageRemovedIntentFilter.addDataScheme("package");
    activity.registerReceiver(broadcastReceiver, packageRemovedIntentFilter);
  }


  protected void initAppsManager() {
    PackageManager packageManager = getPackageManager();
    List<AppInfo> installedApps = getAllInstalledApps(packageManager);

    for(AppInfo installedApp : installedApps) {
      this.allInstalledApps.put(installedApp.getPackageName(), installedApp);

      if(isLaunchableApp(packageManager, installedApp)) {
        this.launchableApps.put(installedApp.getPackageName(), installedApp);
      }
    }

    this.installedAppsRetrieved = true;
  }


  public List<AppInfo> getAllInstalledApps() {
    PackageManager packageManager = getPackageManager();

    return getAllInstalledApps(packageManager);
  }

  protected List<AppInfo> getAllInstalledApps(PackageManager packageManager) {
    List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA);
    List<AppInfo> allInstalledApps = new ArrayList<>(installedPackages.size());

    for(PackageInfo packageInfo : installedPackages) {
      allInstalledApps.add(mapPackageInfoToAppInfo(packageInfo, packageManager));
    }

    return allInstalledApps;
  }

  public List<AppInfo> getLaunchableApps() {
    PackageManager packageManager = getPackageManager();

    return getLaunchableApps(packageManager);
  }

  protected List<AppInfo> getLaunchableApps(PackageManager packageManager) {
    List<AppInfo> launchableInstalledApps = new ArrayList<>();

    List<AppInfo> installedApps = getAllInstalledApps(packageManager);

    for(AppInfo installedApp : installedApps) {
      if(isLaunchableApp(packageManager, installedApp)) {
        launchableInstalledApps.add(installedApp);
      }
    }

    return launchableInstalledApps;
  }

  protected boolean isLaunchableApp(PackageManager packageManager, AppInfo app) {
    return packageManager.getLaunchIntentForPackage(app.getPackageName()) != null;
  }


  public AppInfo getAppInstallationInfo(String packageName) {
    PackageManager packageManager = getPackageManager();

    try {
      PackageInfo installedApplication = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);

      return mapPackageInfoToAppInfo(installedApplication, packageManager);
    } catch(Exception ignored) { } // no app of this package name installed

    return null;
  }


  protected AppInfo mapPackageInfoToAppInfo(PackageInfo packageInfo, PackageManager packageManager) {
    AppInfo app = new AppInfo(packageInfo.packageName);

    app.setTitle((String)packageInfo.applicationInfo.loadLabel(packageManager));
    app.setIconImage(packageInfo.applicationInfo.loadIcon(packageManager));
    app.setAlreadyInstalled(true);
    app.setInstalledVersionString(packageInfo.versionName);

    return app;
  }


  protected PackageManager getPackageManager() {
    return activity.getPackageManager();
  }


  protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      switch(action) {
        case Intent.ACTION_PACKAGE_ADDED:
        case Intent.ACTION_PACKAGE_CHANGED:
          handleAppInstalled(intent);
          break;
        case Intent.ACTION_PACKAGE_REMOVED:
          handleAppRemoved(intent);
          break;
      }
    }
  };

  protected void handleAppInstalled(Intent intent) {
    try {
      String packageName = getPackageNameFromIntent(intent);
      PackageManager packageManager = getPackageManager();
      AppInfo cachedInstalledApp = allInstalledApps.get(packageName);

      if(cachedInstalledApp != null) {
        if(hasAppBeenUpdated(cachedInstalledApp, packageManager)) {
          callAppUpdatedListeners(cachedInstalledApp, isLaunchableApp(packageManager, cachedInstalledApp));
        }
      }
      else {
        AppInfo newlyInstalledApp = getAppInstallationInfo(packageName);
        allInstalledApps.put(packageName, newlyInstalledApp);

        boolean isLaunchable = isLaunchableApp(packageManager, newlyInstalledApp);
        if(isLaunchable) {
          launchableApps.put(packageName, newlyInstalledApp);
        }

        callAppInstalledListeners(newlyInstalledApp, isLaunchable);
      }
    } catch(Exception e) {
      log.error("Could not handle App installed broadcast", e);
    }
  }

  protected void handleAppRemoved(Intent intent) {
    try {
      if (intent.hasExtra(Intent.EXTRA_REPLACING)) { // if EXTRA_REPLACING is set, app is going to be re-installed
        return;
      }

      String packageName = getPackageNameFromIntent(intent);
      AppInfo cachedInstalledApp = allInstalledApps.get(packageName);

      if (cachedInstalledApp != null) {
        allInstalledApps.remove(packageName);
        boolean isLaunchable = launchableApps.remove(packageName) != null;

        callAppRemovedListeners(cachedInstalledApp, isLaunchable);
      }
    } catch(Exception e) {
      log.error("Could not handle App removed broadcast", e);
    }
  }

  protected String getPackageNameFromIntent(Intent intent) {
    String dataString = intent.getDataString();
    return dataString.substring(dataString.indexOf(':') + 1); // remove scheme (= package:)
  }

  protected boolean hasAppBeenUpdated(AppInfo cachedInstalledApp, PackageManager packageManager) {
    AppInfo newAppInfo = getAppInstallationInfo(cachedInstalledApp.getPackageName());

    return cachedInstalledApp.getInstalledVersionString().equals(newAppInfo.getInstalledVersionString()) == false; // TODO: really check if App has been updated
  }


  public boolean addInstalledAppsListener(InstalledAppsListener listener) {
    return installedAppsListeners.add(listener);
  }

  public boolean removeInstalledAppsListener(InstalledAppsListener listener) {
    return installedAppsListeners.remove(listener);
  }

  protected void callAppInstalledListeners(AppInfo newlyInstalledApp, boolean isLaunchable) {
    for(InstalledAppsListener listener : installedAppsListeners) {
      listener.appInstalled(createInstalledAppListenerInfo(newlyInstalledApp, isLaunchable));
    }
  }

  protected void callAppUpdatedListeners(AppInfo updatedApp, boolean isLaunchable) {
    for(InstalledAppsListener listener : installedAppsListeners) {
      listener.appUpdated(createInstalledAppListenerInfo(updatedApp, isLaunchable));
    }
  }

  protected void callAppRemovedListeners(AppInfo removedApp, boolean isLaunchable) {
    for(InstalledAppsListener listener : installedAppsListeners) {
      listener.appRemoved(createInstalledAppListenerInfo(removedApp, isLaunchable));
    }
  }

  protected InstalledAppListenerInfo createInstalledAppListenerInfo(AppInfo installedApp, boolean isLaunchable) {
    List<AppInfo> allInstalledApps = new ArrayList<AppInfo>(this.allInstalledApps.values());
    List<AppInfo> launchableApps = new ArrayList<AppInfo>(this.launchableApps.values());

    return new InstalledAppListenerInfo(installedApp, isLaunchable, allInstalledApps, launchableApps);
  }

}
