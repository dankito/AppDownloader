package net.dankito.appdownloader.responses.callbacks;

import net.dankito.appdownloader.responses.AppDownloadRequestParameters;

/**
 * Created by ganymed on 02/11/16.
 */

public interface GetAppDownloadRequestParametersCallback {

  void completed(AppDownloadRequestParameters requestParameters);

}
