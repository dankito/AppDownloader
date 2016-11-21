package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.downloader.IAppDownloader;

/**
 * Created by ganymed on 02/11/16.
 */

public class GetAppDownloadUrlResponse extends ResponseBase {


  protected AppInfo appToDownload;

  protected IAppDownloader appDownloader;

  protected AppDownloadInfo downloadInfo;

  protected boolean doesNotHaveThisApp = false;



  public GetAppDownloadUrlResponse(AppInfo appToDownload, IAppDownloader appDownloader, String error) {
    super(error);

    this.appToDownload = appToDownload;
    this.appDownloader = appDownloader;
  }

  protected GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload, IAppDownloader appDownloader) {
    super(isSuccessful);

    this.appToDownload = appToDownload;
    this.appDownloader = appDownloader;
  }

  public GetAppDownloadUrlResponse(AppInfo appToDownload, IAppDownloader appDownloader, boolean doesNotHaveThisApp) {
    this(false, appToDownload, appDownloader);

    this.doesNotHaveThisApp = doesNotHaveThisApp;
  }

  public GetAppDownloadUrlResponse(AppInfo appToDownload, IAppDownloader appDownloader, boolean doesNotHaveThisApp, AppDownloadInfo downloadInfo) {
    this(appToDownload, appDownloader, doesNotHaveThisApp);

    this.downloadInfo = downloadInfo;
  }

  public GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload, IAppDownloader appDownloader, AppDownloadInfo downloadInfo) {
    this(isSuccessful, appToDownload, appDownloader);

    this.downloadInfo = downloadInfo;
  }


  public AppInfo getAppToDownload() {
    return appToDownload;
  }

  public IAppDownloader getAppDownloader() {
    return appDownloader;
  }

  public AppDownloadInfo getDownloadInfo() {
    return downloadInfo;
  }

  public boolean isDoesNotHaveThisApp() {
    return doesNotHaveThisApp;
  }

}
