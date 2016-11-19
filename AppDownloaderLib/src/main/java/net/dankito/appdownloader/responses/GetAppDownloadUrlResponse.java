package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.app.AppDownloadInfo;
import net.dankito.appdownloader.app.AppInfo;

/**
 * Created by ganymed on 02/11/16.
 */

public class GetAppDownloadUrlResponse extends ResponseBase {


  protected AppInfo appToDownload;

  protected AppDownloadInfo downloadInfo;



  public GetAppDownloadUrlResponse(AppInfo appToDownload, String error) {
    super(error);

    this.appToDownload = appToDownload;
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

}
