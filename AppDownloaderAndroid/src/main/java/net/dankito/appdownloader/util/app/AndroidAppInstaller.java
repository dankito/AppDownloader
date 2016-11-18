package net.dankito.appdownloader.util.app;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.AppState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 18/11/16.
 */

public class AndroidAppInstaller implements IAppInstaller {

  private static final Logger log = LoggerFactory.getLogger(AndroidAppInstaller.class);


  protected Activity activity;

  protected List<AppInfo> appsBeingInstalled = new CopyOnWriteArrayList<>();


  public AndroidAppInstaller(Activity activity) {
    this.activity = activity;

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
    activity.startActivityForResult(intent, -1);

    appsBeingInstalled.add(appToInstall);
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

      for(AppInfo appBeingInstalled : new ArrayList<>(appsBeingInstalled)) {
        if(appBeingInstalled.getPackageName().equals(changedAppPackage)) {
          deletedDownloadedApk(appBeingInstalled);

          appsBeingInstalled.remove(appBeingInstalled);
          appBeingInstalled.setState(AppState.UPDATABLE);
          break;
        }
      }
    }
  }

  protected void deletedDownloadedApk(AppInfo appBeingInstalled) {
    try {
      File file = new File(appBeingInstalled.getDownloadLocationPath());
      if(file.exists()) {
        log.info("Deleting installed Apk file " + file.getAbsolutePath());
        file.delete();
      }
    } catch(Exception e) {
      log.error("Could not deleted installed Apk file " + appBeingInstalled.getDownloadLocationPath(), e);
    }
  }

}
