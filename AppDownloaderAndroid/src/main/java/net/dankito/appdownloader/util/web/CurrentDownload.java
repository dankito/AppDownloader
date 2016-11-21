package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.app.model.AppDownloadInfo;

/**
 * Created by ganymed on 18/11/16.
 */

public class CurrentDownload {

  protected AppDownloadInfo downloadInfo;

  protected IDownloadCompletedCallback callback;


  public CurrentDownload(AppDownloadInfo downloadInfo, IDownloadCompletedCallback callback) {
    this.downloadInfo = downloadInfo;
    this.callback = callback;
  }


  public AppDownloadInfo getDownloadInfo() {
    return downloadInfo;
  }

  public IDownloadCompletedCallback getCallback() {
    return callback;
  }


  @Override
  public String toString() {
    return "" + downloadInfo;
  }

}
