package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.app.AppDownloadInfo;
import net.dankito.appdownloader.responses.ResponseBase;

/**
 * Created by ganymed on 18/11/16.
 */
public class DownloadResult extends ResponseBase {

  protected AppDownloadInfo downloadInfo;

  protected boolean isUserCancelled;


  public DownloadResult(AppDownloadInfo downloadInfo, boolean successful) {
    super(successful);
    this.downloadInfo = downloadInfo;
  }

  public DownloadResult(AppDownloadInfo downloadInfo, boolean isSuccessful, boolean isUserCancelled) {
    super(isSuccessful);
    this.downloadInfo = downloadInfo;
    this.isUserCancelled = isUserCancelled;
  }

  public DownloadResult(AppDownloadInfo downloadInfo, String error) {
    super(error);
    this.downloadInfo = downloadInfo;
  }


  public AppDownloadInfo getDownloadInfo() {
    return downloadInfo;
  }

  public boolean isUserCancelled() {
    return isUserCancelled;
  }

}
