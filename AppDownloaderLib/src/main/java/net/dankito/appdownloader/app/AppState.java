package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 14/11/16.
 */

public enum AppState {

  INSTALLABLE,
  UPDATABLE,
  GETTING_DOWNLOAD_URL,
  DOWNLOADING,
  VERIFYING_SIGNATURE,
  INSTALLING

}
