package net.dankito.appdownloader.app.apkverifier.virustotal.calback;

import net.dankito.appdownloader.app.apkverifier.virustotal.response.GetFileScanResponse;

/**
 * Created by ganymed on 04/12/16.
 */

public interface GetFileScanCallback {

  void completed(GetFileScanResponse response);

}
