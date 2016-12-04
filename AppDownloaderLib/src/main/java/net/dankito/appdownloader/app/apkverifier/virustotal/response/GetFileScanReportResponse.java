package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import net.dankito.appdownloader.app.apkverifier.virustotal.ResponseCode;

/**
 * Created by ganymed on 04/12/16.
 */

public class GetFileScanReportResponse extends ApiCallResponseBase {

  protected VirusTotalFileScanReportResponse response;


  public GetFileScanReportResponse(ResponseCode responseCode, String error) {
    super(responseCode, error);
  }

  public GetFileScanReportResponse(ResponseCode responseCode, VirusTotalFileScanReportResponse response) {
    super(responseCode, true);
    this.response = response;
  }


  public VirusTotalFileScanReportResponse getResponse() {
    return response;
  }

}
