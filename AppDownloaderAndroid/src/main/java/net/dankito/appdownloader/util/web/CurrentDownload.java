package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.app.AppDownloadLink;

/**
 * Created by ganymed on 18/11/16.
 */

public class CurrentDownload {

  protected AppDownloadLink downloadLink;

  protected IDownloadCompletedCallback callback;


  public CurrentDownload(AppDownloadLink downloadLink, IDownloadCompletedCallback callback) {
    this.downloadLink = downloadLink;
    this.callback = callback;
  }


  public AppDownloadLink getDownloadLink() {
    return downloadLink;
  }

  public IDownloadCompletedCallback getCallback() {
    return callback;
  }


  @Override
  public String toString() {
    return "" + downloadLink;
  }

}
