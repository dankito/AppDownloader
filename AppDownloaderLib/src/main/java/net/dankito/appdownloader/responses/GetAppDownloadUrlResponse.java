package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.app.AppDownloadInfo;
import net.dankito.appdownloader.app.AppInfo;

/**
 * Created by ganymed on 02/11/16.
 */

public class GetAppDownloadUrlResponse extends ResponseBase {


  protected AppInfo appToDownload;

  protected AppDownloadInfo downloadInfo;

  protected boolean doesNotHaveThisApp = false;



  public GetAppDownloadUrlResponse(AppInfo appToDownload, String error) {
    super(error);

    this.appToDownload = appToDownload;
  }

  public GetAppDownloadUrlResponse(AppInfo appToDownload, boolean doesNotHaveThisApp) {
    super(false);

    this.appToDownload = appToDownload;
    this.doesNotHaveThisApp = doesNotHaveThisApp;
  }

  protected GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload) {
    super(isSuccessful);
    this.appToDownload = appToDownload;
  }

  public GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload, AppDownloadInfo downloadInfo) {
    this(isSuccessful, appToDownload);
    this.downloadInfo = downloadInfo;
  }


  public AppInfo getAppToDownload() {
    return appToDownload;
  }

  public AppDownloadInfo getDownloadInfo() {
    return downloadInfo;
  }

  public boolean isDoesNotHaveThisApp() {
    return doesNotHaveThisApp;
  }

}
