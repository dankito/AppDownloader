package net.dankito.appdownloader.util.web;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;

import net.dankito.appdownloader.responses.AppSearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by ganymed on 05/11/16.
 */

public class AndroidDownloadManager extends BroadcastReceiver implements IDownloadManager {

  protected static final DateFormat DOWNLOAD_TIME_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd_hh-mm-ss");

  protected static final Character[] RESERVED_CHARACTERS = {'|', '\\', '?', '*', '<', '\'', '"', ':', '>', '[', ']', '/'};

  private static final Logger log = LoggerFactory.getLogger(AndroidDownloadManager.class);


  protected Activity context;

  protected List<Long> currentDownloads = new CopyOnWriteArrayList<>();


  public AndroidDownloadManager(Activity context) {
    this.context = context;

    registerBroadcastReceivers(context);
  }

  private void registerBroadcastReceivers(Activity context) {
    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));

    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_VIEW_DOWNLOADS));
  }


  @Override
  public void downloadUrlAsync(AppSearchResult appSearchResult, String url) {
    try {
      Uri uri = Uri.parse(url);
      DownloadManager.Request request = new DownloadManager.Request(uri);

      // TODO: make configurable if allowed to download over celluar network and on roaming
      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
      request.setAllowedOverRoaming(false);

      request.setTitle(appSearchResult.getTitle());
      request.setDescription("Downloading " + appSearchResult.getTitle());

      String destinationFileName = getDestinationFilename(appSearchResult, url);
      request.setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, destinationFileName);

      // enqueue this request
      DownloadManager downloadManager = getDownloadManager();
      currentDownloads.add(downloadManager.enqueue(request));
    } catch(Exception e) {
      log.error("Could not start Download for " + appSearchResult, e);
    }
  }

  @NonNull
  protected String getDestinationFilename(AppSearchResult appSearchResult, String url) {
    return removeReservedCharacters(appSearchResult.getTitle()) + "_" +  DOWNLOAD_TIME_DATE_FORMAT.format(new Date()) + ".apk";

//    try {
//      URL downloadUrl = new URL(url);
//      File file = new File(downloadUrl.getPath());
//      return file.getName() + "_" + DOWNLOAD_TIME_DATE_FORMAT.format(new Date()) + ".apk";
//    } catch(Exception e) {
//      log.error("Could not extract destination filename from " + url);
//    }
  }

  protected String removeReservedCharacters(String filename) {
    for(char reservedCharacter : RESERVED_CHARACTERS){
      filename = filename.replace(reservedCharacter, '_');
    }

    return filename;
  }


  @Override
  public void onReceive(Context context, Intent intent) {
    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
    String action = intent.getAction();

    switch(action) {
      case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
        handleDownloadCompleteBroadcast(downloadId);
        break;
      case DownloadManager.ACTION_NOTIFICATION_CLICKED:
        handleNotificationClickedBroadcast(downloadId);
        break;
      case DownloadManager.ACTION_VIEW_DOWNLOADS:
        handleViewDownloadsBroadcast(downloadId);
        break;
    }
  }

  protected void handleDownloadCompleteBroadcast(long downloadId) {
    if(currentDownloads.contains(downloadId)) { // yeah, we started this download
      currentDownloads.remove(downloadId);

      try {
        DownloadManager downloadManager = getDownloadManager();
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);

        if(cursor != null && cursor.moveToFirst()) {
          String downloadLocation = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI));

          installApp(downloadLocation);

          cursor.close();
        }
      } catch(Exception e) {
        log.error("Could not handle successful Download", e);
      }
    }
  }

  protected void installApp(String downloadLocation) {
    Intent intent = new Intent();
    intent.setAction(android.content.Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.fromFile(new File(downloadLocation)), "application/vnd.android.package-archive");
    context.startActivityForResult(intent, 10);
  }


  protected void handleNotificationClickedBroadcast(long downloadId) {

  }

  protected void handleViewDownloadsBroadcast(long downloadId) {

  }


  protected DownloadManager getDownloadManager() {
    return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
  }

}
