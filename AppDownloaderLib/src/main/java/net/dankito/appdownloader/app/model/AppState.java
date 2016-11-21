package net.dankito.appdownloader.app.model;

/**
 * Created by ganymed on 14/11/16.
 */

public enum AppState {

  INSTALLABLE,
  REINSTALLABLE,
  UPDATABLE,
  GETTING_DOWNLOAD_URL,
  DOWNLOADING,
  VERIFYING_SIGNATURE,
  INSTALLING

}
