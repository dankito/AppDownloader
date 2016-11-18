package net.dankito.appdownloader.util.web;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.AppState;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URI;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 05/11/16.
 */

public class AndroidDownloadManager extends BroadcastReceiver implements IDownloadManager {

  protected static final DateFormat DOWNLOAD_TIME_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd_hh-mm-ss");

  protected static final Character[] RESERVED_CHARACTERS = {'|', '\\', '?', '*', '<', '\'', '"', ':', '>', '[', ']', '/'};

  private static final Logger log = LoggerFactory.getLogger(AndroidDownloadManager.class);


  protected Activity context;

  // TODO: save currentDownloaded so that on an App restart aborted and on restart finished downloads can be handled
  protected Map<Long, AppInfo> currentDownloads = new ConcurrentHashMap<>();

  protected List<AppInfo> appsBeingInstalled = new CopyOnWriteArrayList<>();


  public AndroidDownloadManager(Activity context) {
    this.context = context;

    registerBroadcastReceivers(context);
  }

  private void registerBroadcastReceivers(Activity context) {
    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

    IntentFilter packageAddedIntentFilter = new IntentFilter(Intent.ACTION_PACKAGE_ADDED);
    packageAddedIntentFilter.addDataScheme("package"); // this seems to be absolutely necessary for PACKAGE_ADDED, see https://stackoverflow.com/questions/11246326/how-to-receiving-broadcast-when-application-installed-or-removed
    context.registerReceiver(this, packageAddedIntentFilter);
  }


  @Override
  public void downloadUrlAsync(AppInfo appInfo, String url) {
    try {
      Uri uri = Uri.parse(url);
      DownloadManager.Request request = new DownloadManager.Request(uri);

      // TODO: make configurable if allowed to download over cellular network and on roaming
      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
      request.setAllowedOverRoaming(false);

      request.setTitle(appInfo.getTitle());
      request.setDescription(appInfo.getTitle());

      String destinationFileName = getDestinationFilename(appInfo, url);
      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, destinationFileName);

      DownloadManager downloadManager = getDownloadManager();
      currentDownloads.put(downloadManager.enqueue(request), appInfo);
    } catch(Exception e) {
      log.error("Could not start Download for " + appInfo, e);
    }
  }

  @NonNull
  protected String getDestinationFilename(AppInfo appInfo, String url) {
    return removeReservedCharacters(appInfo.getTitle()) + "_" +  DOWNLOAD_TIME_DATE_FORMAT.format(new Date()) + ".apk";
  }

  protected String removeReservedCharacters(String filename) {
    for(char reservedCharacter : RESERVED_CHARACTERS){
      filename = filename.replace(reservedCharacter, '_');
    }

    return filename;
  }


  @Override
  public void onReceive(Context context, Intent intent) {
    String action = intent.getAction();

    switch(action) {
      case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
        handleDownloadCompleteBroadcast(intent);
        break;
      case DownloadManager.ACTION_NOTIFICATION_CLICKED:
        handleNotificationClickedBroadcast(intent);
        break;
      case Intent.ACTION_PACKAGE_ADDED:
      case Intent.ACTION_PACKAGE_CHANGED:
        handlePackageAddedOrChanged(intent);
        break;
    }
  }

  protected void handleDownloadCompleteBroadcast(Intent intent) {
    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

    if(currentDownloads.containsKey(downloadId)) { // yeah, we started this download
      AppInfo appHavingDownloaded = currentDownloads.remove(downloadId);

      try {
        EnqueuedDownload enqueuedDownload = getEnqueuedDownloadForId(downloadId);

        if(enqueuedDownload != null) {
          if(enqueuedDownload.wasDownloadSuccessful()) {
            appHavingDownloaded.setDownloadLocationUri(enqueuedDownload.getDownloadLocationUri());

            // TODO: call listener, installApp is not a task of DownloadManager
            installApp(appHavingDownloaded, enqueuedDownload.getDownloadLocationUri());
          }
        }
      } catch(Exception e) {
        log.error("Could not handle successful Download", e);
      }
    }
  }

  protected EnqueuedDownload getEnqueuedDownloadForId(long downloadId) {
    DownloadManager downloadManager = getDownloadManager();
    DownloadManager.Query query = new DownloadManager.Query();
    query.setFilterById(downloadId);
    Cursor cursor = downloadManager.query(query);

    if(cursor != null && cursor.moveToFirst()) {
      EnqueuedDownload result = deserializeDatabaseEntry(cursor);

      cursor.close();

      return result;
    }

    return null;
  }

  protected EnqueuedDownload deserializeDatabaseEntry(Cursor cursor) {
    EnqueuedDownload result = new EnqueuedDownload();

    result.setId(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_ID)));

    result.setUri(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_URI)));
    result.setDownloadLocationUri(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));

    result.setFileSize(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES)));
    result.setBytesDownloadedSoFar(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));

    result.setStatus(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)));
    result.setReason(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_REASON)));

    result.setTitle(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_TITLE)));
    result.setDescription(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_DESCRIPTION)));

    result.setLastModified(new Date(cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP))));

    result.setMediaType(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIA_TYPE)));
    result.setMediaProviderUri(cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_MEDIAPROVIDER_URI)));

    return result;
  }

  protected void installApp(AppInfo appToInstall, String downloadLocation) {
    appToInstall.setState(AppState.INSTALLING);

    Intent intent = new Intent();
    intent.setAction(android.content.Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.parse(downloadLocation), "application/vnd.android.package-archive");
    context.startActivityForResult(intent, -1);

    appsBeingInstalled.add(appToInstall);
  }


  protected void handleNotificationClickedBroadcast(final Intent intent) {
    long[] downloadIds = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);

    for(final long downloadId : downloadIds) {
      askShouldDownloadGetCancelled(downloadId);
    }
  }

  protected void askShouldDownloadGetCancelled(final long downloadId) {
    AppInfo appToStop = currentDownloads.get(downloadId);
    String appTitle = appToStop == null ? "" : appToStop.getTitle();

    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder = builder.setMessage(context.getString(R.string.alert_message_cancel_download, appTitle));

    builder.setNegativeButton(R.string.no, null);
    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        cancelDownload(downloadId);
      }
    });

    builder.create().show();
  }

  protected void cancelDownload(long downloadId) {
    DownloadManager downloadManager = getDownloadManager();

    downloadManager.remove(downloadId);

    currentDownloads.remove(downloadId);
  }


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
      URI uri = URI.create(appBeingInstalled.getDownloadLocationUri()); // get File from Uri
      File file = new File(uri.getPath());
      if(file.exists()) {
        log.info("Deleting installed Apk file " + file.getAbsolutePath());
        file.delete();
      }
    } catch(Exception e) {
      log.error("Could not deleted installed Apk file " + appBeingInstalled.getDownloadLocationUri(), e);
    }
  }


  protected DownloadManager getDownloadManager() {
    return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
  }

}
