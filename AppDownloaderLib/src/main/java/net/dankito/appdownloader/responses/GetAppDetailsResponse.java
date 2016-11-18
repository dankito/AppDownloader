package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 04/11/16.
 */

public class GetAppDetailsResponse extends ResponseBase {

  protected AppInfo appInfo;


  public GetAppDetailsResponse(AppInfo appInfo, String error) {
    super(error);
    this.appInfo = appInfo;
  }

  public GetAppDetailsResponse(AppInfo appInfo, boolean isSuccessful) {
    super(isSuccessful);
    this.appInfo = appInfo;
  }

}
