package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.HashAlgorithm;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.GetUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.responses.callbacks.GetUrlCallback;
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
 * Created by ganymed on 19/11/16.
 */

public class ApkMirrorPlayStoreAppDownloader extends AppDownloaderBase {

  protected static final String SEARCH_APPS_URL_PREFIX = "https://www.apkmirror.com/?s=";
  protected static final String SEARCH_APPS_URL_SUFFIX = "&post_type=app_release&searchtype=apk";

  protected static final String DETAILS_PAGE_URL_PREFIX = "https://www.apkmirror.com";

  private static final Logger log = LoggerFactory.getLogger(ApkMirrorPlayStoreAppDownloader.class);


  public ApkMirrorPlayStoreAppDownloader(IWebClient webClient, IThreadPool threadPool) {
    super(webClient, threadPool);
  }


  @Override
  public void getAppDownloadLinkAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback) {
    getAppDetailsPageUrlAsync(appToDownload, new GetUrlCallback() {
      @Override
      public void completed(GetUrlResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          successfullyRetrievedAppDetailsPageUrl(appToDownload, response, callback);
        }
      }
    });
  }

  protected void successfullyRetrievedAppDetailsPageUrl(final AppInfo appToDownload, GetUrlResponse response, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = new RequestParameters(response.getUrl());

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
  }

  protected void parseAppDetailsPage(AppInfo appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    Document document = Jsoup.parse(response.getBody());

    Elements seeAvailableApkElements = document.body().select(".variantsButton");
    if(seeAvailableApkElements.size() > 0) { // in this case there are several apks to select from
      getRealAppDetailsPageUrl(appToDownload, seeAvailableApkElements.first().parent().parent().parent(), callback);
    }
    else {
      parseAppDetailsPage(appToDownload, document, callback);
    }
  }

  protected void parseAppDetailsPage(AppInfo appToDownload, Document document, GetAppDownloadUrlResponseCallback callback) {
    AppDownloadLink downloadLink = new AppDownloadLink(appToDownload, this);
    String appDownloadPageUrl = null;

    Elements downloadButtonElements = document.body().select(".downloadButton");
    if(downloadButtonElements.size() > 0) {
      Element downloadButtonElement = downloadButtonElements.first();
      appDownloadPageUrl = DETAILS_PAGE_URL_PREFIX + downloadButtonElement.attr("href");
    }

    Elements fingerprintElements = document.body().select("span.wordbreak-all");
    if(fingerprintElements.size() > 0) {
      Element fingerprintElement = fingerprintElements.first();
      String apkSignature = fingerprintElement.text().replace(":", "");

      downloadLink.setApkSignature(apkSignature);
    }

    parseApkDetailTable(downloadLink, document);

    if(appDownloadPageUrl == null) {
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find App Download Page Url")); // TODO: translate
    }
    else {
      getAppDownloadPageUrl(appToDownload, downloadLink, appDownloadPageUrl, callback);
    }
  }

  protected void parseApkDetailTable(AppDownloadLink downloadLink, Document document) {
    Elements appDetailTableElements = document.body().select(".apk-detail-table");
    if(appDetailTableElements.size() > 0) {
      Element appDetailTableElement = appDetailTableElements.first();

      for(Element child : appDetailTableElement.children()) {
        if(child.hasClass("appspec-row")) {
          parseAppSpecRowElement(downloadLink, child);
        }
      }
    }
  }

  protected void parseAppSpecRowElement(AppDownloadLink downloadLink, Element appSpecRowElement) {
    Element appSpecTitleElement = appSpecRowElement.select(".appspec-icon").first();
    Element appSpecValueElement = appSpecRowElement.select(".appspec-value").first();

    if(appSpecTitleElement != null && appSpecValueElement != null) {
      String appSpecificationTitle = appSpecTitleElement.attr("title");
      String appSpecificationValue = appSpecValueElement.text();

      if("APK file size".equals(appSpecificationTitle)) {
        downloadLink.setFileSize(appSpecificationValue);
      }
      else if("MD5 signature".equals(appSpecificationTitle)) {
        downloadLink.setHashAlgorithm(HashAlgorithm.MD5);
        downloadLink.setFileHashSum(appSpecificationValue);
      }
    }
  }

  protected void getRealAppDetailsPageUrl(AppInfo appToDownload, Element contentElement, GetAppDownloadUrlResponseCallback callback) {
    Elements tagIconElements = contentElement.select(".tag-icon");
    if(tagIconElements.size() > 0) {
      Element tagIconElement = tagIconElements.first();
      Element tagIconElementParent = tagIconElement.parent();

      if("a".equals(tagIconElementParent.nodeName())) {
        String realAppDetailsPageUrl = tagIconElementParent.attr("href");
        realAppDetailsPageUrl = DETAILS_PAGE_URL_PREFIX + realAppDetailsPageUrl;

        successfullyRetrievedAppDetailsPageUrl(appToDownload, new GetUrlResponse(true, realAppDetailsPageUrl), callback);
        return;
      }
    }

    callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find App Details Page Url")); // TODO: translate
  }


  protected void getAppDownloadPageUrl(final AppInfo appToDownload, final AppDownloadLink downloadLink, final String appDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = new RequestParameters(appDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, response.getError()));
        }
        else {
          parseAppDownloadPage(appToDownload, downloadLink, response, callback);
        }
      }
    });
  }

  protected void parseAppDownloadPage(AppInfo appToDownload, AppDownloadLink downloadLink, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    Document document = Jsoup.parse(response.getBody());

    Elements clickHereElements = document.body().select("a[data-google-vignette]");
    if(clickHereElements.size() > 0) {
      Element clickHereElement = clickHereElements.first();
      String appDownloadUrl = DETAILS_PAGE_URL_PREFIX + clickHereElement.attr("href");
      downloadLink.setUrl(appDownloadUrl);

      appToDownload.addDownloadUrl(downloadLink);

      callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, downloadLink));
      return;
    }

    callback.completed(new GetAppDownloadUrlResponse(appToDownload, "Could not find App Download Url")); // TODO: translate
  }


  protected void getAppDetailsPageUrlAsync(final AppInfo appToDownload, final GetUrlCallback callback) {
    try {
      String url = SEARCH_APPS_URL_PREFIX + URLEncoder.encode(appToDownload.getPackageName(), "ASCII") + SEARCH_APPS_URL_SUFFIX;
      RequestParameters parameters = createRequestParametersWithDefaultValues(url);

      webClient.getAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new GetUrlResponse(response.getError()));
          }
          else {
            parseAppSearchResultPage(response, callback);
          }
        }
      });
    } catch(Exception e) {
      log.error("Could not get Download Link for " + appToDownload, e);
      callback.completed(new GetUrlResponse(e.getLocalizedMessage()));
    }
  }

  protected void parseAppSearchResultPage(WebClientResponse response, GetUrlCallback callback) {
    Document document = Jsoup.parse(response.getBody());
    Elements tabContainerElements = document.body().select(".tab-container");

    if(tabContainerElements.size() > 0) {
      Element tabContainerElement = tabContainerElements.first();

      Elements downloadIconElements = tabContainerElement.parent().select(".download-icon");

      if(downloadIconElements.size() > 0) {
        Element downloadIconElement = downloadIconElements.first();
        Element downloadIconElementParent = downloadIconElement.parent();
        if("a".equals(downloadIconElementParent.nodeName())) {
          String appDetailsPageUrl = downloadIconElementParent.attr("href");
          appDetailsPageUrl = DETAILS_PAGE_URL_PREFIX + appDetailsPageUrl;

          callback.completed(new GetUrlResponse(true, appDetailsPageUrl));
          return;
        }
      }
    }

    callback.completed(new GetUrlResponse("Could not find App Details Page Url")); // TODO: translate
  }
}
