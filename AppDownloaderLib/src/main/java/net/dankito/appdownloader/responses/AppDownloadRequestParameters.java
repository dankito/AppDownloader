package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 02/11/16.
 */

public class AppDownloadRequestParameters extends ResponseBase {

  protected String appPackageParameterName;

  protected String appPackage;

  protected String timestampParameterName;

  protected String timestamp;

  protected String tokenParameterName;

  protected String token;


  public AppDownloadRequestParameters(String appPackage, String error) {
    super(error);
    this.appPackage = appPackage;
  }

  public AppDownloadRequestParameters(String appPackageParameterName, String appPackageName, String timestamp, String tokenParameterName, String token) {
    this(appPackageParameterName, appPackageName, "t", timestamp, tokenParameterName, token);
  }

  public AppDownloadRequestParameters(String appPackageParameterName, String appPackageName, String timestampParameterName, String timestamp,
                                      String tokenParameterName, String token) {
    super(true);

    this.appPackageParameterName = appPackageParameterName;
    this.appPackage = appPackageName;

    this.timestampParameterName = timestampParameterName;
    this.timestamp = timestamp;

    this.tokenParameterName = tokenParameterName;
    this.token = token;
  }


  public String getAppPackageParameterName() {
    return appPackageParameterName;
  }

  public String getAppPackage() {
    return appPackage;
  }

  public String getTimestampParameterName() {
    return timestampParameterName;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getTokenParameterName() {
    return tokenParameterName;
  }

  public String getToken() {
    return token;
  }


  public String getRequestBodyString() {
    return getAppPackageParameterName() + "=" + getAppPackage() + "&" +
        getTimestampParameterName() + "=" + getTimestamp() + "&" +
        getTokenParameterName() + "=" + getToken() + "&fetch=false";
  }

}
