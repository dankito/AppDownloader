package net.dankito.appdownloader.app.apkverifier.virustotal;

/**
 * Created by ganymed on 03/12/16.
 */

public enum ResponseCode {

  ITEM_NOT_FOUND(0),

  ITEM_FOUND(1),

  QUERIED_FOR_ANALYSIS(-2);


  private int responseCode;

  ResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }


  public int getCode() {
    return responseCode;
  }

}
