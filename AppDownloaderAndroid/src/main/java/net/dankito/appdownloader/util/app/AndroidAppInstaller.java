package net.dankito.appdownloader.util.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import net.dankito.appdownloader.MainActivity;
import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.AppState;
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

  protected Map<Integer, AppDownloadLink> appsBeingInstalled = new ConcurrentHashMap<>();


  public AndroidAppInstaller(Activity activity) {
    this.activity = activity;

    ((MainActivity)activity).registerActivityResultListener(APP_INSTALL_REQUEST_CODE, installAppActivityResultListener);

    registerBroadcastReceivers(activity);
  }


  protected void registerBroadcastReceivers(Activity activity) {
    IntentFilter packageAddedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    packageAddedIntentFilter.addDataScheme("package"); // this seems to be absolutely necessary for PACKAGE_ADDED, see https://stackoverflow.com/questions/11246326/how-to-receiving-broadcast-when-application-installed-or-removed
    activity.registerReceiver(broadcastReceiver, packageAddedIntentFilter);
  }


  public void installApp(AppDownloadLink downloadLink) {
    AppInfo appToInstall = downloadLink.getAppInfo();
    appToInstall.setState(AppState.INSTALLING);

    Intent intent = new Intent();
    intent.setAction(android.content.Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.parse(downloadLink.getDownloadLocationUri()), "application/vnd.android.package-archive");

    int requestCode = APP_INSTALL_REQUEST_CODE + CountInstalledApps++;
    activity.startActivityForResult(intent, requestCode);

    appsBeingInstalled.put(requestCode, downloadLink);
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

      for(Map.Entry<Integer, AppDownloadLink> appBeingInstalledEntry : new ArrayList<>(appsBeingInstalled.entrySet())) {
        AppDownloadLink downloadLink = appBeingInstalledEntry.getValue();
        AppInfo appBeingInstalled = downloadLink.getAppInfo();

        if(appBeingInstalled.getPackageName().equals(changedAppPackage)) {
          appSuccessfullyInstalled(appBeingInstalledEntry.getKey(), appBeingInstalled, downloadLink);
          break;
        }
      }
    }
  }

  protected void appSuccessfullyInstalled(Integer appRequestCode, AppInfo appBeingInstalled, AppDownloadLink downloadLink) {
    appsBeingInstalled.remove(appRequestCode);

    appBeingInstalled.setAlreadyInstalled(true);
    appBeingInstalled.setInstalledVersion(appBeingInstalled.getVersion());

    appBeingInstalled.setToItsDefaultState();

    deletedDownloadedApk(downloadLink);
  }

  protected void deletedDownloadedApk(AppDownloadLink downloadLink) {
    try {
      File file = new File(downloadLink.getDownloadLocationPath());
      if(file.exists()) {
        log.info("Deleting installed Apk file " + file.getAbsolutePath());
        file.delete();
      }
    } catch(Exception e) {
      log.error("Could not deleted installed Apk file " + downloadLink.getDownloadLocationPath(), e);
    }
  }


  protected IActivityResultListener installAppActivityResultListener = new IActivityResultListener() {
    @Override
    public void receivedActivityResult(int requestCode, int resultCode, Intent data) {
      doneInstallingApp(requestCode, resultCode, data);
    }
  };

  protected void doneInstallingApp(int requestCode, int resultCode, Intent data) {
    AppDownloadLink downloadLink = appsBeingInstalled.remove(requestCode);
    AppInfo appBeingInstalled = downloadLink.getAppInfo();

    if(appBeingInstalled != null) { // it may has already been removed by handlePackageAddedOrChanged()
      appBeingInstalled.setToItsDefaultState();

      deletedDownloadedApk(downloadLink);
    }
  }

}
