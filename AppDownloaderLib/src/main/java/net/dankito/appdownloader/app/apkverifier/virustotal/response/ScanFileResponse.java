package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import net.dankito.appdownloader.app.apkverifier.virustotal.ResponseCode;

/**
 * Created by ganymed on 04/12/16.
 */

public class ScanFileResponse extends ApiCallResponseBase {

  protected boolean isSuccessfullyQueued;

  protected VirusTotalFileScanResponse response;


  public ScanFileResponse(ResponseCode responseCode, String error) {
    super(responseCode, error);
  }

  public ScanFileResponse(ResponseCode responseCode, boolean isSuccessfullyQueued, VirusTotalFileScanResponse response) {
    super(responseCode, isSuccessfullyQueued);
    this.isSuccessfullyQueued = isSuccessfullyQueued;
    this.response = response;
  }


  public boolean isSuccessfullyQueued() {
    return isSuccessfullyQueued;
  }

  public VirusTotalFileScanResponse getResponse() {
    return response;
  }

}
