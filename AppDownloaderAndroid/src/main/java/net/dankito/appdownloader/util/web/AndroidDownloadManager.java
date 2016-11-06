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
import net.dankito.appdownloader.responses.AppSearchResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ganymed on 05/11/16.
 */

public class AndroidDownloadManager extends BroadcastReceiver implements IDownloadManager {

  protected static final DateFormat DOWNLOAD_TIME_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd_hh-mm-ss");

  protected static final Character[] RESERVED_CHARACTERS = {'|', '\\', '?', '*', '<', '\'', '"', ':', '>', '[', ']', '/'};

  private static final Logger log = LoggerFactory.getLogger(AndroidDownloadManager.class);


  protected Activity context;

  // TODO: save currentDownloaded so that on an App restart aborted and on restart finished downloads can be handled
  protected Map<Long, AppSearchResult> currentDownloads = new ConcurrentHashMap<>();


  public AndroidDownloadManager(Activity context) {
    this.context = context;

    registerBroadcastReceivers(context);
  }

  private void registerBroadcastReceivers(Activity context) {
    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

    context.registerReceiver(this, new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
  }


  @Override
  public void downloadUrlAsync(AppSearchResult appSearchResult, String url) {
    try {
      Uri uri = Uri.parse(url);
      DownloadManager.Request request = new DownloadManager.Request(uri);

      // TODO: make configurable if allowed to download over cellular network and on roaming
      request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);
      request.setAllowedOverRoaming(false);

      request.setTitle(appSearchResult.getTitle());
      request.setDescription(appSearchResult.getTitle());

      String destinationFileName = getDestinationFilename(appSearchResult, url);
      request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, destinationFileName);

      DownloadManager downloadManager = getDownloadManager();
      currentDownloads.put(downloadManager.enqueue(request), appSearchResult);
    } catch(Exception e) {
      log.error("Could not start Download for " + appSearchResult, e);
    }
  }

  @NonNull
  protected String getDestinationFilename(AppSearchResult appSearchResult, String url) {
    return removeReservedCharacters(appSearchResult.getTitle()) + "_" +  DOWNLOAD_TIME_DATE_FORMAT.format(new Date()) + ".apk";
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
    }
  }

  protected void handleDownloadCompleteBroadcast(Intent intent) {
    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

    if(currentDownloads.containsKey(downloadId)) { // yeah, we started this download
      currentDownloads.remove(downloadId);

      try {
        EnqueuedDownload enqueuedDownload = getEnqueuedDownloadForId(downloadId);

        if(enqueuedDownload != null) {
          installApp(enqueuedDownload.getDownloadLocationUri());
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

  protected void installApp(String downloadLocation) {
    Intent intent = new Intent();
    intent.setAction(android.content.Intent.ACTION_VIEW);
    intent.setDataAndType(Uri.parse(downloadLocation), "application/vnd.android.package-archive");
    context.startActivityForResult(intent, 10);
  }


  protected void handleNotificationClickedBroadcast(final Intent intent) {
    long[] downloadIds = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);

    for(final long downloadId : downloadIds) {
      askShouldDownloadGetCancelled(downloadId);
    }
  }

  protected void askShouldDownloadGetCancelled(final long downloadId) {
    AppSearchResult appToStop = currentDownloads.get(downloadId);
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


  protected DownloadManager getDownloadManager() {
    return (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
  }

}
