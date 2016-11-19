package net.dankito.appdownloader.app;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class AndroidInstalledAppsManager implements IInstalledAppsManager {


  protected Activity activity;


  public AndroidInstalledAppsManager(Activity activity) {
    this.activity = activity;
  }


  public List<AppInfo> getInstalledApps() {
    PackageManager packageManager = getPackageManager();

    return getLaunchableApps(packageManager);
  }

  protected List<PackageInfo> getAllInstalledApps(PackageManager packageManager) {
    return packageManager.getInstalledPackages(PackageManager.GET_PERMISSIONS | PackageManager.GET_META_DATA);
  }

  protected List<AppInfo> getLaunchableApps(PackageManager packageManager) {
    List<AppInfo> launchableInstalledApps = new ArrayList<>();

    List<PackageInfo> installedPackageInfos = getAllInstalledApps(packageManager);

    for(PackageInfo installedPackage : installedPackageInfos) {
      if(isLaunchableApp(packageManager, installedPackage)) {
        launchableInstalledApps.add(mapPackageInfoToAppInfo(installedPackage, packageManager));
      }
    }

    return launchableInstalledApps;
  }

  protected boolean isLaunchableApp(PackageManager packageManager, PackageInfo packageInfo) {
    return packageManager.getLaunchIntentForPackage(packageInfo.packageName) != null;
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
    app.setInstalledVersion(packageInfo.versionName);

    return app;
  }


  protected PackageManager getPackageManager() {
    return activity.getPackageManager();
  }

}
