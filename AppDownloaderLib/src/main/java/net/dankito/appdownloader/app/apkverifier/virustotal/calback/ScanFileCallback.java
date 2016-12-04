package net.dankito.appdownloader.app.apkverifier.virustotal.calback;

import net.dankito.appdownloader.app.apkverifier.virustotal.response.ScanFileResponse;

/**
 * Created by ganymed on 04/12/16.
 */

public interface ScanFileCallback {

  void completed(ScanFileResponse response);

}
