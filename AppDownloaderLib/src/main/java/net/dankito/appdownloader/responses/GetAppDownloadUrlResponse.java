package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;

/**
 * Created by ganymed on 02/11/16.
 */

public class GetAppDownloadUrlResponse extends ResponseBase {


  protected AppInfo appToDownload;

  protected AppDownloadLink downloadLink;



  public GetAppDownloadUrlResponse(AppInfo appToDownload, String error) {
    super(error);

    this.appToDownload = appToDownload;
  }

  protected GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload) {
    super(isSuccessful);
    this.appToDownload = appToDownload;
  }

  public GetAppDownloadUrlResponse(boolean isSuccessful, AppInfo appToDownload, AppDownloadLink downloadLink) {
    this(isSuccessful, appToDownload);
    this.downloadLink = downloadLink;
  }


  public AppInfo getAppToDownload() {
    return appToDownload;
  }

  public AppDownloadLink getDownloadLink() {
    return downloadLink;
  }

}
