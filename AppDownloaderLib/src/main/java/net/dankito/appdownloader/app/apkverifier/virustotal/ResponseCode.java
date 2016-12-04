package net.dankito.appdownloader.app.apkverifier.virustotal;

/**
 * Created by ganymed on 03/12/16.
 */

public enum ResponseCode {

  ITEM_NOT_FOUND(0),

  ITEM_FOUND(1),

  QUERIED_FOR_ANALYSIS(-2),

  NETWORK_ERROR(-100),

  PARSE_ERROR(-101),

  UNKNOWN(199);


  private int responseCode;

  ResponseCode(int responseCode) {
    this.responseCode = responseCode;
  }


  public int getCode() {
    return responseCode;
  }


  public static ResponseCode fromCode(int code) {
    if(ITEM_NOT_FOUND.getCode() == code) {
      return ITEM_NOT_FOUND;
    }
    else if(ITEM_FOUND.getCode() == code) {
      return ITEM_FOUND;
    }
    else if(QUERIED_FOR_ANALYSIS.getCode() == code) {
      return QUERIED_FOR_ANALYSIS;
    }

    return UNKNOWN;
  }

}
