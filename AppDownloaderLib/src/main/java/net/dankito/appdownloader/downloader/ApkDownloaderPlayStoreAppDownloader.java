package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.HashAlgorithm;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
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


  public ApkDownloaderPlayStoreAppDownloader(IWebClient webClient) {
    super(webClient);
  }


  public void getAppDownloadLinkAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback) {
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

  protected void parseAppDetailsPage(AppInfo appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      String responseBody = response.getBody();
      Document document = Jsoup.parse(responseBody);


      Elements detailsElements = document.body().select(".details");
      if(detailsElements.size() > 0) {
        Element detailsElement = detailsElements.first();

        AppDownloadLink appDownloadLink = parseAppDownloadFileDetails(appToDownload, detailsElement);

        Elements downloadAnchorElements = detailsElement.select("a.mdl-button");
        if (downloadAnchorElements.size() > 0) {
          Element downloadAnchorElement = downloadAnchorElements.first();
          String appDownloadPageUrl = downloadAnchorElement.attr("href");
          appDownloadPageUrl = "https://apk-dl.com" + appDownloadPageUrl;

          downloadAndParseAppDownloadPage(appToDownload, appDownloadLink, appDownloadPageUrl, callback);
          return;
        }
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected AppDownloadLink parseAppDownloadFileDetails(AppInfo appToDownload, Element detailsElement) {
    AppDownloadLink appDownloadLink = new AppDownloadLink(appToDownload, this);

    for(Element detailChild : detailsElement.children()) {
      if("div".equals(detailChild.nodeName()) && detailChild.children().size() > 0 && "span".equals(detailChild.child(0).nodeName())) {
        Element detailsSpanElement = detailChild.child(0);
        String downloadFileDetailsName = detailsSpanElement.text();
        String downloadFileDetailsValue = detailsSpanElement.nextSibling().toString(); // second child node is the text node with details' value

        if(downloadFileDetailsName.startsWith("File Size")) {
          appDownloadLink.setFileSize(downloadFileDetailsValue.trim());
        }
        else if(downloadFileDetailsName.startsWith("File Sha")) {
          appDownloadLink.setHashAlgorithm(HashAlgorithm.SHA1);
          appDownloadLink.setFileHashSum(downloadFileDetailsValue.trim());
        }
        else if(downloadFileDetailsName.startsWith("APK Signature")) {
          appDownloadLink.setApkSignature(downloadFileDetailsValue.trim());
        }
      }
    }

    return appDownloadLink;
  }

  protected void downloadAndParseAppDownloadPage(final AppInfo appToDownload, final AppDownloadLink appDownloadLink, final String appDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDownloadPage(appToDownload, appDownloadLink, response, callback);
        }
      }
    });
  }

  protected void parseAppDownloadPage(AppInfo appToDownload, AppDownloadLink appDownloadLink, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      Elements clickHereSpans = document.select("span.glyphicon-cloud-download");
      if(clickHereSpans.size() > 0) {
        Element downloadAnchor = clickHereSpans.first().parent();

        if("a".equals(downloadAnchor.nodeName())) {
          String appDetailDownloadPageUrl = downloadAnchor.attr("href");
          downloadAndParseAppDetailDownloadPage(appToDownload, appDownloadLink, appDetailDownloadPageUrl, callback);
          return;
        }
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected void downloadAndParseAppDetailDownloadPage(final AppInfo appToDownload, final AppDownloadLink appDownloadLink, String appDetailDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDetailDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDetailDownloadPage(appToDownload, appDownloadLink, response, callback);
        }
      }
    });
  }

  protected void parseAppDetailDownloadPage(AppInfo appToDownload, AppDownloadLink appDownloadLink, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      Elements downloadAnchors = document.select("a.mdl-button");
      if(downloadAnchors.size() > 0) {
        Element downloadAnchor = downloadAnchors.first();

        String appDownloadUrl = downloadAnchor.attr("href");
        appDownloadUrl = "http:" + appDownloadUrl;

        appDownloadLink.setUrl(appDownloadUrl);
        appToDownload.addDownloadUrl(appDownloadLink);

        callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, appDownloadLink));
        return;
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

}
