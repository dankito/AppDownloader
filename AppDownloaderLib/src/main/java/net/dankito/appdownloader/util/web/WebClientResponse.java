package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.responses.ResponseBase;

/**
 * Created by ganymed on 03/11/16.
 */

public class WebClientResponse extends ResponseBase {

  protected int httpStatusCode;

  protected String body;


  public WebClientResponse(String error) {
    super(error);
  }

  public WebClientResponse(boolean isSuccessful) {
    super(isSuccessful);
  }

  public WebClientResponse(int httpStatusCode) {
    this(httpStatusCode == 200);

    this.httpStatusCode = httpStatusCode;
  }

  public WebClientResponse(int httpStatusCode, String body) {
    this(httpStatusCode);

    this.body = body;
  }


  public int getHttpStatusCode() {
    return httpStatusCode;
  }

  public String getBody() {
    return body;
  }

}
