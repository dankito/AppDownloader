package net.dankito.appdownloader.responses;

import java.io.File;

/**
 * Created by ganymed on 04/11/16.
 */

public class DownloadAppResponse extends ResponseBase {

  protected AppSearchResult appToDownload;

  protected File downloadLocation;


  public DownloadAppResponse(String error) {
    super(error);
  }

  public DownloadAppResponse(AppSearchResult appToDownload, File downloadLocation) {
    super(true);
    this.appToDownload = appToDownload;
    this.downloadLocation = downloadLocation;
  }


  public AppSearchResult getAppToDownload() {
    return appToDownload;
  }

  public File getDownloadLocation() {
    return downloadLocation;
  }

}
