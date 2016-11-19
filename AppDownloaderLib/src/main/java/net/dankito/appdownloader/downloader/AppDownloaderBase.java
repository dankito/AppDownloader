package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestParameters;

/**
 * Created by ganymed on 04/11/16.
 */

public abstract class AppDownloaderBase implements IAppDownloader {

  public static final int DOWNLOAD_CONNECTION_TIMEOUT_MILLIS = 2000;

  public static final int COUNT_CONNECTION_RETRIES = 2;


  protected IWebClient webClient;


  public AppDownloaderBase(IWebClient webClient) {
    this.webClient = webClient;
  }


  protected RequestParameters createRequestParametersWithDefaultValues(String url) {
    RequestParameters parameters = new RequestParameters(url);
    parameters.setConnectionTimeoutMillis(DOWNLOAD_CONNECTION_TIMEOUT_MILLIS);
    parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);
    return parameters;
  }

}
