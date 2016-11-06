package net.dankito.appdownloader.responses.callbacks;

import net.dankito.appdownloader.responses.DownloadAppResponse;

/**
 * Created by ganymed on 04/11/16.
 */

public interface DownloadAppCallback {

  void completed(DownloadAppResponse response);

}
