package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 02/11/16.
 */

public class GetAppDownloadUrlResponse extends ResponseBase {


  protected AppSearchResult appToDownload;

  protected String url;



  public GetAppDownloadUrlResponse(AppSearchResult appToDownload, String error) {
    super(error);

    this.appToDownload = appToDownload;
  }

  protected GetAppDownloadUrlResponse(boolean isSuccessful, AppSearchResult appToDownload) {
    super(isSuccessful);
    this.appToDownload = appToDownload;
  }

  public GetAppDownloadUrlResponse(boolean isSuccessful, AppSearchResult appToDownload, String url) {
    this(isSuccessful, appToDownload);
    this.url = url;
  }


  public AppSearchResult getAppToDownload() {
    return appToDownload;
  }

  public String getUrl() {
    return url;
  }

}
