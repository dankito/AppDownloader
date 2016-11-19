package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 19/11/16.
 */

public class GetUrlResponse extends ResponseBase {

  protected String url;


  public GetUrlResponse(String error) {
    super(error);
  }

  public GetUrlResponse(boolean isSuccessful, String url) {
    super(isSuccessful);
    this.url = url;
  }


  public String getUrl() {
    return url;
  }

}
