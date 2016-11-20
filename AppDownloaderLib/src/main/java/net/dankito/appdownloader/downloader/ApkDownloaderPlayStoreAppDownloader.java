package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppDownloadInfo;
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


  @Override
  public int getTrustworthinessFactor() {
    return TRUSTWORTHINESS_NOT_SURE;
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

        AppDownloadInfo appDownloadInfo = parseAppDownloadFileDetails(appToDownload, detailsElement);

        Elements downloadAnchorElements = detailsElement.select("a.mdl-button.mdl-button--colored");
        if(downloadAnchorElements.size() > 0) {
          downloadAndParseAppDownloadPage(appToDownload, appDownloadInfo, downloadAnchorElements.first(), callback);
        }
        else { // no download link available
          // in rare cases like this there are file information like apk signature and file checksum available but no download link
          appToDownload.addDownloadUrl(appDownloadInfo);

          callback.completed(new GetAppDownloadUrlResponse(appToDownload, true));
        }

        return;
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected AppDownloadInfo parseAppDownloadFileDetails(AppInfo appToDownload, Element detailsElement) {
    AppDownloadInfo appDownloadInfo = new AppDownloadInfo(appToDownload, this);

    for(Element detailChild : detailsElement.children()) {
      if("div".equals(detailChild.nodeName()) && detailChild.children().size() > 0 && "span".equals(detailChild.child(0).nodeName())) {
        Element detailsSpanElement = detailChild.child(0);
        String downloadFileDetailsName = detailsSpanElement.text();
        String downloadFileDetailsValue = detailsSpanElement.nextSibling().toString(); // second child node is the text node with details' value

        if(downloadFileDetailsName.startsWith("File Size")) {
          appDownloadInfo.setFileSize(downloadFileDetailsValue.trim());
        }
        else if(downloadFileDetailsName.startsWith("File Sha")) {
          appDownloadInfo.setFileHashAlgorithm(HashAlgorithm.SHA1);
          appDownloadInfo.setFileChecksum(downloadFileDetailsValue.trim());
        }
        else if(downloadFileDetailsName.startsWith("APK Signature")) {
          appDownloadInfo.setApkSignature(downloadFileDetailsValue.trim());
        }
      }
    }

    return appDownloadInfo;
  }

  protected void downloadAndParseAppDownloadPage(AppInfo appToDownload, AppDownloadInfo appDownloadInfo, Element downloadAnchorElement, GetAppDownloadUrlResponseCallback callback) {
    String appDownloadPageUrl = downloadAnchorElement.attr("href");
    appDownloadPageUrl = "https://apk-dl.com" + appDownloadPageUrl;

    downloadAndParseAppDownloadPage(appToDownload, appDownloadInfo, appDownloadPageUrl, callback);
  }

  protected void downloadAndParseAppDownloadPage(final AppInfo appToDownload, final AppDownloadInfo appDownloadInfo, final String appDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDownloadPage(appToDownload, appDownloadInfo, response, callback);
        }
      }
    });
  }

  protected void parseAppDownloadPage(AppInfo appToDownload, AppDownloadInfo appDownloadInfo, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      Elements clickHereSpans = document.select("span.glyphicon-cloud-download");
      if(clickHereSpans.size() > 0) {
        Element downloadAnchor = clickHereSpans.first().parent();

        if("a".equals(downloadAnchor.nodeName())) {
          String appDetailDownloadPageUrl = downloadAnchor.attr("href");
          downloadAndParseAppDetailDownloadPage(appToDownload, appDownloadInfo, appDetailDownloadPageUrl, callback);
          return;
        }
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected void downloadAndParseAppDetailDownloadPage(final AppInfo appToDownload, final AppDownloadInfo appDownloadInfo, String appDetailDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDetailDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDetailDownloadPage(appToDownload, appDownloadInfo, response, callback);
        }
      }
    });
  }

  protected void parseAppDetailDownloadPage(AppInfo appToDownload, AppDownloadInfo appDownloadInfo, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      Elements downloadAnchors = document.select("a.mdl-button");
      if(downloadAnchors.size() > 0) {
        Element downloadAnchor = downloadAnchors.first();

        String appDownloadUrl = downloadAnchor.attr("href");
        appDownloadUrl = "http:" + appDownloadUrl;

        appDownloadInfo.setUrl(appDownloadUrl);
        appToDownload.addDownloadUrl(appDownloadInfo);

        callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, appDownloadInfo));
        return;
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find download Link")); // TODO: translate
    } catch(Exception e) {
      log.error("Could not parse App Download Page for " + appToDownload, e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

}
