package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.app.AppInfo;

/**
 * Created by ganymed on 02/11/16.
 */

public class GetAppDownloadUrlResponse extends ResponseBase {


  protected AppInfo appToDownload;

  protected String url;



  public GetAppDownloadUrlResponse(AppInfo appToDownload, String error) {
    super(error);

    this.appToDownload = appToDownload;
  }

  protected GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload) {
    super(isSuccessful);
    this.appToDownload = appToDownload;
  }

  public GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload, String url) {
    this(isSuccessful, appToDownload);
    this.url = url;
  }


  public AppInfo getAppToDownload() {
    return appToDownload;
  }

  public String getUrl() {
    return url;
  }

}
