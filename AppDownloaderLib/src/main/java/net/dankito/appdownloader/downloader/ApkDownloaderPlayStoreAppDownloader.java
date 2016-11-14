package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.responses.AppSearchResult;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.IThreadPool;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestCallback;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;

/**
 * Created by ganymed on 02/11/16.
 */

public class ApkDownloaderPlayStoreAppDownloader extends AppDownloaderBase {

  protected static final String APK_DOWNLOADER_APP_DETAILS_PAGE_URL = "https://apk-dl.com/";

  private static final Logger log = LoggerFactory.getLogger(ApkDownloaderPlayStoreAppDownloader.class);


  public ApkDownloaderPlayStoreAppDownloader(IWebClient webClient, IThreadPool threadPool) {
    super(webClient, threadPool);
  }


  public void getAppDownloadLinkAsync(final AppSearchResult appToDownload, final GetAppDownloadUrlResponseCallback callback) {
    try {
      String url = APK_DOWNLOADER_APP_DETAILS_PAGE_URL + URLEncoder.encode(appToDownload.getPackageName(), "ASCII");
      RequestParameters parameters = createRequestParametersWithDefaultValues(url);

      webClient.getAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
          }
          else {
            parseAppDetailsPage(appToDownload, response, callback);
          }
        }
      });
    } catch(Exception e) {
      log.error("Could not get Download Link for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected void parseAppDetailsPage(AppSearchResult appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      String responseBody = response.getBody();
      Document document = Jsoup.parse(responseBody);

      Elements downloadDivElements = document.body().select(".download-btn");
      if (downloadDivElements.size() > 0) {
        Element downloadDivElement = downloadDivElements.first();

        Elements downloadAnchorElements = downloadDivElement.select("a.mdl-button");
        if (downloadAnchorElements.size() > 0) {
          Element downloadAnchorElement = downloadAnchorElements.first();
          String appDownloadPageUrl = downloadAnchorElement.attr("href");
          appDownloadPageUrl = "https://apk-dl.com" + appDownloadPageUrl;

          downloadAndParseAppDownloadPage(appToDownload, appDownloadPageUrl, callback);
          return;
        }
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected void downloadAndParseAppDownloadPage(final AppSearchResult appToDownload, final String appDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDownloadPage(appToDownload, response, callback);
        }
      }
    });
  }

  protected void parseAppDownloadPage(AppSearchResult appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      Elements clickHereSpans = document.select("span.glyphicon-cloud-download");
      if(clickHereSpans.size() > 0) {
        Element downloadAnchor = clickHereSpans.first().parent();

        if("a".equals(downloadAnchor.nodeName())) {
          String appDetailDownloadPageUrl = downloadAnchor.attr("href");
          downloadAndParseAppDetailDownloadPage(appToDownload, appDetailDownloadPageUrl, callback);
          return;
        }
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected void downloadAndParseAppDetailDownloadPage(final AppSearchResult appToDownload, String appDetailDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDetailDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDetailDownloadPage(appToDownload, response, callback);
        }
      }
    });
  }

  protected void parseAppDetailDownloadPage(AppSearchResult appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      Elements downloadAnchors = document.select("a.mdl-button");
      if(downloadAnchors.size() > 0) {
        Element downloadAnchor = downloadAnchors.first();

        String appDownloadUrl = downloadAnchor.attr("href");
        appDownloadUrl = "http:" + appDownloadUrl;

        callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, appDownloadUrl));
        return;
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

}
