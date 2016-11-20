package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestParameters;

/**
 * Created by ganymed on 04/11/16.
 */

public abstract class AppDownloaderBase implements IAppDownloader {

  // two additional download sources would be https://apkplz.com and https://sameapk.com.
  // But they either require executing JavaScript (https://sameapk.com) or user interaction (reading a captcha; https://apkplz.com).


  public static final int TRUSTWORTHINESS_ABSOLUTE_TRUSTWORTHY = 1000;

  public static final int TRUSTWORTHINESS_TRUSTWORTHY = 100;

  public static final int TRUSTWORTHINESS_NOT_SURE = 10;


  public static final int DOWNLOAD_CONNECTION_TIMEOUT_MILLIS = 2000;

  public static final int COUNT_CONNECTION_RETRIES = 2;


  protected IWebClient webClient;


  public AppDownloaderBase(IWebClient webClient) {
    this.webClient = webClient;
  }


  @Override
  public boolean isTrustworthySource() {
    return getTrustworthinessFactor() >= TRUSTWORTHINESS_TRUSTWORTHY;
  }

  protected abstract int getTrustworthinessFactor();


  protected RequestParameters createRequestParametersWithDefaultValues(String url) {
    RequestParameters parameters = new RequestParameters(url);
    parameters.setConnectionTimeoutMillis(DOWNLOAD_CONNECTION_TIMEOUT_MILLIS);
    parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);
    return parameters;
  }

}
