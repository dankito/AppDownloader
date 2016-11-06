package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 04/11/16.
 */

public class GetAppDetailsResponse extends ResponseBase {

  protected AppSearchResult appSearchResult;


  public GetAppDetailsResponse(AppSearchResult appSearchResult, String error) {
    super(error);
    this.appSearchResult = appSearchResult;
  }

  public GetAppDetailsResponse(AppSearchResult appSearchResult, boolean isSuccessful) {
    super(isSuccessful);
    this.appSearchResult = appSearchResult;
  }

}
