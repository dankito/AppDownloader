package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import net.dankito.appdownloader.app.apkverifier.virustotal.ResponseCode;
import net.dankito.appdownloader.responses.ResponseBase;

/**
 * Created by ganymed on 04/12/16.
 */

public class VirusTotalResponseBase extends ResponseBase {

  protected ResponseCode responseCode;


  public VirusTotalResponseBase(ResponseCode responseCode, String error) {
    super(error);
    this.responseCode = responseCode;
  }

  protected VirusTotalResponseBase(ResponseCode responseCode, boolean isSuccessful) {
    super(isSuccessful);
    this.responseCode = responseCode;
  }


  public ResponseCode getResponseCode() {
    return responseCode;
  }

}
