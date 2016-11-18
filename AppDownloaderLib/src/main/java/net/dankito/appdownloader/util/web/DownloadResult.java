package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.responses.ResponseBase;

/**
 * Created by ganymed on 18/11/16.
 */
public class DownloadResult extends ResponseBase {

  protected AppDownloadLink downloadLink;

  protected boolean isUserCancelled;


  public DownloadResult(AppDownloadLink downloadLink, boolean successful) {
    super(successful);
  }

  public DownloadResult(AppDownloadLink downloadLink, boolean isSuccessful, boolean isUserCancelled) {
    super(isSuccessful);
    this.downloadLink = downloadLink;
    this.isUserCancelled = isUserCancelled;
  }

  public DownloadResult(AppDownloadLink downloadLink, String error) {
    super(error);
    this.downloadLink = downloadLink;
  }


  public AppDownloadLink getDownloadLink() {
    return downloadLink;
  }

  public boolean isUserCancelled() {
    return isUserCancelled;
  }

}
