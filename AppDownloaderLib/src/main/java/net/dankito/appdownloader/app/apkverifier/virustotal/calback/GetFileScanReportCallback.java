package net.dankito.appdownloader.app.apkverifier.virustotal.calback;

import net.dankito.appdownloader.app.apkverifier.virustotal.response.GetFileScanReportResponse;

/**
 * Created by ganymed on 04/12/16.
 */

public interface GetFileScanReportCallback {

  void completed(GetFileScanReportResponse response);

}
