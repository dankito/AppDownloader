package net.dankito.appdownloader.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import net.dankito.appdownloader.MainActivity;
import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.AppState;
import net.dankito.appdownloader.util.android.IActivityResultListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 18/11/16.
 */

public class AndroidAppInstaller implements IAppInstaller {

  public static final int APP_INSTALL_REQUEST_CODE = 2703;

  protected static int CountInstalledApps = 0;


  private static final Logger log = LoggerFactory.getLogger(AndroidAppInstaller.class);


  protected Activity activity;

  protected Map<Integer, AppDownloadInfo> appsBeingInstalled = new ConcurrentHashMap<>();


  public AndroidAppInstaller(Activity activity) {
    this.activity = activity;

    registerBroadcastReceivers(activity);
  }


  protected void registerBroadcastReceivers(Activity activity) {
    IntentFilter packageAddedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    packageAddedIntentFilter.addDataScheme("package"); // this seems to be absolutely necessary for PACKAGE_ADDED, see https://stackoverflow.com/questions/11246326/how-to-receiving-broadcast-when-application-installed-or-removed
    activity.registerReceiver(broadcastReceiver, packageAddedIntentFilter);
  }


  public void installApp(AppDownloadInfo downloadInfo) {
    AppInfo appToInstall = downloadInfo.getAppInfo();
    appToInstall.setState(AppState.INSTALLING);

    Intent intent = new Intent();
    intent.setAction(android.content.Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.parse(downloadInfo.getDownloadLocationUri()), "application/vnd.android.package-archive");

    int requestCode = APP_INSTALL_REQUEST_CODE + CountInstalledApps++;
    registerActivityResultListener(requestCode);
    activity.startActivityForResult(intent, requestCode);

    appsBeingInstalled.put(requestCode, downloadInfo);
  }


  protected BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
    @Override
    public void onReceive(Context context, Intent intent) {
      String action = intent.getAction();

      switch(action) {
        case Intent.ACTION_PACKAGE_ADDED:
        case Intent.ACTION_PACKAGE_CHANGED:
          handlePackageAddedOrChanged(intent);
          break;
      }
    }
  };


  protected void handlePackageAddedOrChanged(Intent intent) {
    if(appsBeingInstalled.size() > 0) {
      String dataString = intent.getDataString();
      String changedAppPackage = dataString.substring(dataString.indexOf(':') + 1); // remove scheme (= package:)

      for(Map.Entry<Integer, AppDownloadInfo> appBeingInstalledEntry : new ArrayList<>(appsBeingInstalled.entrySet())) {
        AppDownloadInfo downloadInfo = appBeingInstalledEntry.getValue();
        AppInfo appBeingInstalled = downloadInfo.getAppInfo();

        if(appBeingInstalled.getPackageName().equals(changedAppPackage)) {
          appSuccessfullyInstalled(appBeingInstalledEntry.getKey(), appBeingInstalled, downloadInfo);
          break;
        }
      }
    }
  }

  protected void appSuccessfullyInstalled(Integer appRequestCode, AppInfo appBeingInstalled, AppDownloadInfo downloadInfo) {
    appsBeingInstalled.remove(appRequestCode);

    appBeingInstalled.setAlreadyInstalled(true);
    appBeingInstalled.setInstalledVersionString(appBeingInstalled.getVersionString());

    appBeingInstalled.setToItsDefaultState();

    deletedDownloadedApk(downloadInfo);

    unregisterActivityResultListener(appRequestCode);
  }

  protected void deletedDownloadedApk(AppDownloadInfo downloadInfo) {
    try {
      File file = new File(downloadInfo.getDownloadLocationPath());
      if(file.exists()) {
        log.info("Deleting installed Apk file " + file.getAbsolutePath());
        file.delete();
      }
    } catch(Exception e) {
      log.error("Could not deleted installed Apk file " + downloadInfo.getDownloadLocationPath(), e);
    }
  }


  protected void registerActivityResultListener(int requestCode) {
    ((MainActivity)activity).registerActivityResultListener(requestCode, installAppActivityResultListener);
  }

  protected void unregisterActivityResultListener(int requestCode) {
    ((MainActivity)activity).unregisterActivityResultListener(requestCode);
  }

  protected IActivityResultListener installAppActivityResultListener = new IActivityResultListener() {
    @Override
    public void receivedActivityResult(int requestCode, int resultCode, Intent data) {
      doneInstallingApp(requestCode, resultCode, data);
    }
  };

  protected void doneInstallingApp(int requestCode, int resultCode, Intent data) {
    AppDownloadInfo downloadInfo = appsBeingInstalled.remove(requestCode);
    AppInfo appBeingInstalled = downloadInfo.getAppInfo();

    if(appBeingInstalled != null) { // it may has already been removed by handlePackageAddedOrChanged()
      appBeingInstalled.setToItsDefaultState();

      deletedDownloadedApk(downloadInfo);
    }

    unregisterActivityResultListener(requestCode);
  }

}
