package net.dankito.appdownloader.downloader;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.HashAlgorithm;
import net.dankito.appdownloader.responses.AppDownloadRequestParameters;
import net.dankito.appdownloader.responses.EvoziGetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadRequestParametersCallback;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestCallback;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by ganymed on 02/11/16.
 */

public class EvoziPlayStoreAppDownloader extends AppDownloaderBase {

  protected static final String APPS_EVOZI_START_PAGE_URL = "https://apps.evozi.com/apk-downloader/";
  protected static final String APPS_EVOZI_DOWNLOAD_APP_URL = "https://api-apk-6.evozi.com/download";

  private static final Logger log = LoggerFactory.getLogger(EvoziPlayStoreAppDownloader.class);


  protected ObjectMapper mapper = new ObjectMapper();


  public EvoziPlayStoreAppDownloader(IWebClient webClient) {
    super(webClient);
  }


  public void getAppDownloadLinkAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback) {
    getAppDownloadRequestParametersAsync(appToDownload.getPackageName(), new GetAppDownloadRequestParametersCallback() {
      @Override
      public void completed(AppDownloadRequestParameters requestParameters) {
        if(requestParameters.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, requestParameters.getError()));
        }
        else {
          requestAppDownloadUrl(appToDownload, requestParameters, callback);
        }
      }
    });
  }

  protected void requestAppDownloadUrl(final AppInfo appToDownload, final AppDownloadRequestParameters requestParameters, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(APPS_EVOZI_DOWNLOAD_APP_URL);
    parameters.setBody(requestParameters.getRequestBodyString());

    webClient.postAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          receivedAppDownloadUrlResponse(appToDownload, response.getBody(), callback);
        }
      }
    });
  }

  protected void receivedAppDownloadUrlResponse(AppInfo appToDownload, String getAppDownloadUrlResponseBody, final GetAppDownloadUrlResponseCallback callback) {
    try {
      EvoziGetAppDownloadUrlResponse evoziAppDownloadUrlResponse = mapper.readValue(getAppDownloadUrlResponseBody, EvoziGetAppDownloadUrlResponse.class);
      log.info("Retrieved Download Url for " + appToDownload + ": " + evoziAppDownloadUrlResponse.getUrl());

      if(evoziAppDownloadUrlResponse.hasErrorOccurred()) {
        callback.completed(new GetAppDownloadUrlResponse(appToDownload, evoziAppDownloadUrlResponse.getData()));
      }
      else {
        AppDownloadLink appDownloadLink = new AppDownloadLink(appToDownload, this);
        appDownloadLink.setUrl(evoziAppDownloadUrlResponse.getUrl());
        appDownloadLink.setFileSize(evoziAppDownloadUrlResponse.getFilesize());
        appDownloadLink.setHashAlgorithm(HashAlgorithm.MD5);
        appDownloadLink.setFileHashSum(evoziAppDownloadUrlResponse.getMd5());

        appToDownload.addDownloadUrl(appDownloadLink);

        GetAppDownloadUrlResponse appDownloadUrlResponse = new GetAppDownloadUrlResponse(true, appToDownload, appDownloadLink);

        callback.completed(appDownloadUrlResponse);
      }
    } catch(Exception e) {
      log.error("Could not parse GetAppDownloadUrlResponse for App Package " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected void getAppDownloadRequestParametersAsync(final String appPackageName, final GetAppDownloadRequestParametersCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(APPS_EVOZI_START_PAGE_URL);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new AppDownloadRequestParameters(appPackageName, response.getError()));
        }
        else {
          readAppDownloadRequestParameters(response.getBody(), appPackageName, callback);
        }
      }
    });
  }

  protected void readAppDownloadRequestParameters(String getTokensResponseBody, String appPackageName, GetAppDownloadRequestParametersCallback callback) {
    try {
      AppDownloadRequestParameters requestParameters = readAppDownloadRequestParameters(getTokensResponseBody, appPackageName);
      log.info("Retrieved App Download Request Parameters for " + appPackageName + ": " + requestParameters.getRequestBodyString());

      callback.completed(requestParameters);
    } catch(Exception e) {
      log.error("Could not read App Download Request Parameters for App Package " + appPackageName, e);
      callback.completed(new AppDownloadRequestParameters(appPackageName, e.getLocalizedMessage()));
    }
  }

  protected AppDownloadRequestParameters readAppDownloadRequestParameters(String getTokensResponseBody, String appPackageName) throws IOException {
    int packageParameterNameEndIndex = getTokensResponseBody.indexOf(": packagename, t: ");
    int packageParameterNameStartIndex = getTokensResponseBody.lastIndexOf('{', packageParameterNameEndIndex) + 1;
    String appPackageParameterName = getTokensResponseBody.substring(packageParameterNameStartIndex, packageParameterNameEndIndex).trim();


    int timestampStartIndex = packageParameterNameEndIndex + ": packagename, t: ".length();
    int timestampEndIndex = getTokensResponseBody.indexOf(",", timestampStartIndex + 1);
    String timestamp = getTokensResponseBody.substring(timestampStartIndex, timestampEndIndex);

    int tokenKeyEndIndex = getTokensResponseBody.indexOf(":", timestampEndIndex + 1);
    String tokenParameterName = getTokensResponseBody.substring(timestampEndIndex + 1, tokenKeyEndIndex).trim();

    // token is not directly stated here but by a variable name
    int tokenReferrerEndIndex = getTokensResponseBody.indexOf(',', tokenKeyEndIndex + 1);
    String tokenReferrer = getTokensResponseBody.substring(tokenKeyEndIndex + 1, tokenReferrerEndIndex).trim();

    // somewhere else in the response a JS variable with token value is declared: var <tokenReferrer> = '<tokenValue>'
    String tokenVariable = "var " + tokenReferrer + " = \'";
    int tokenValueStartIndex = getTokensResponseBody.indexOf(tokenVariable) + tokenVariable.length();
    int tokenValueEndIndex = getTokensResponseBody.indexOf("\'", tokenValueStartIndex + 1);
    String tokenValue = getTokensResponseBody.substring(tokenValueStartIndex, tokenValueEndIndex);

    // TODO: may also read app package and timestamp parameter names
    return new AppDownloadRequestParameters(appPackageParameterName, appPackageName, timestamp, tokenParameterName, tokenValue);
  }

}
