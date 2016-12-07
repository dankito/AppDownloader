package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.HashAlgorithm;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.GetUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.responses.callbacks.GetUrlCallback;
import net.dankito.appdownloader.util.StringUtils;
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

  protected static final String BASE_URL = "https://www.apkmirror.com";

  private static final Logger log = LoggerFactory.getLogger(ApkMirrorPlayStoreAppDownloader.class);


  public ApkMirrorPlayStoreAppDownloader(IWebClient webClient) {
    super(webClient);
  }


  @Override
  public int getTrustworthinessFactor() {
    return TRUSTWORTHINESS_TRUSTWORTHY;
  }

  @Override
  public void getAppDownloadLinkAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback) {
    getAppDetailsPageUrlAsync(appToDownload, callback, new GetUrlCallback() {
      @Override
      public void completed(GetUrlResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, ApkMirrorPlayStoreAppDownloader.this, response.getError()));
        }
        else {
          successfullyRetrievedAppDetailsPageUrl(appToDownload, response, callback);
        }
      }
    });
  }

  protected void successfullyRetrievedAppDetailsPageUrl(final AppInfo appToDownload, GetUrlResponse response, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(response.getUrl());

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, ApkMirrorPlayStoreAppDownloader.this, response.getError()));
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
    AppDownloadInfo downloadInfo = new AppDownloadInfo(appToDownload, this);
    String appDownloadPageUrl = null;

    Elements downloadButtonElements = document.body().select(".downloadButton");
    if(downloadButtonElements.size() > 0) {
      Element downloadButtonElement = downloadButtonElements.first();
      appDownloadPageUrl = BASE_URL + downloadButtonElement.attr("href");
    }

    parseAppDetails(downloadInfo, document);

    if(appDownloadPageUrl == null) { // TODO: check for variants, as e.g. on https://www.apkmirror.com/apk/opera-software-asa/opera/opera-37-0-2192-105088-release/
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, this, "Could not find App Download Page Url")); // TODO: translate
    }
    else {
      if(appDownloadPageUrl.contains("/download.php?id=")) { // this is already the download link
        downloadInfo.setUrl(appDownloadPageUrl);
        callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, this, downloadInfo));
      }
      else {
        getAppDownloadPageUrl(appToDownload, downloadInfo, appDownloadPageUrl, callback);
      }
    }
  }

  protected void parseAppDetails(AppDownloadInfo downloadInfo, Document document) {
    Elements fingerprintElements = document.body().select("span.wordbreak-all");
    if(fingerprintElements.size() > 0) {
      Element fingerprintElement = fingerprintElements.first();
      String apkSignature = fingerprintElement.text().replace(":", "");

      downloadInfo.setApkSignature(apkSignature);
    }

    parseApkDetailTable(downloadInfo, document);
  }

  protected void parseApkDetailTable(AppDownloadInfo downloadInfo, Document document) {
    Elements appDetailTableElements = document.body().select(".apk-detail-table");
    if(appDetailTableElements.size() > 0) {
      Element appDetailTableElement = appDetailTableElements.first();

      for(Element child : appDetailTableElement.children()) {
        if(child.hasClass("appspec-row")) {
          parseAppSpecRowElement(downloadInfo, child);
        }
      }
    }
  }

  protected void parseAppSpecRowElement(AppDownloadInfo downloadInfo, Element appSpecRowElement) {
    Element appSpecTitleElement = appSpecRowElement.select(".appspec-icon").first();
    Element appSpecValueElement = appSpecRowElement.select(".appspec-value").first();

    if(appSpecTitleElement != null && appSpecValueElement != null) {
      String appSpecificationTitle = appSpecTitleElement.attr("title");
      String appSpecificationValue = appSpecValueElement.text();

      if("APK file size".equals(appSpecificationTitle)) {
        downloadInfo.setFileSize(appSpecificationValue);
      }
      else if("MD5 signature".equals(appSpecificationTitle)) {
        downloadInfo.setFileHashAlgorithm(HashAlgorithm.MD5);
        downloadInfo.setFileChecksum(appSpecificationValue);
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
        realAppDetailsPageUrl = BASE_URL + realAppDetailsPageUrl;

        successfullyRetrievedAppDetailsPageUrl(appToDownload, new GetUrlResponse(true, realAppDetailsPageUrl), callback);
        return;
      }
    }

    callback.completed(new GetAppDownloadUrlResponse(appToDownload, this, "Could not find App Details Page Url")); // TODO: translate
  }


  protected void getAppDownloadPageUrl(final AppInfo appToDownload, final AppDownloadInfo downloadInfo, final String appDownloadPageUrl, final GetAppDownloadUrlResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(appDownloadPageUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetAppDownloadUrlResponse(appToDownload, ApkMirrorPlayStoreAppDownloader.this, response.getError()));
        }
        else {
          parseAppDownloadPage(appToDownload, downloadInfo, response, callback);
        }
      }
    });
  }

  protected void parseAppDownloadPage(AppInfo appToDownload, AppDownloadInfo downloadInfo, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    Document document = Jsoup.parse(response.getBody());

    Elements clickHereElements = document.body().select("a[data-google-vignette]");
    if(clickHereElements.size() > 0) {
      Element clickHereElement = clickHereElements.first();
      String appDownloadUrl = BASE_URL + clickHereElement.attr("href");
      downloadInfo.setUrl(appDownloadUrl);

      callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, this, downloadInfo));
      return;
    }

    callback.completed(new GetAppDownloadUrlResponse(appToDownload, this, "Could not find App Download Url")); // TODO: translate
  }


  protected void getAppDetailsPageUrlAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback getAppDownloadUrlResponseCallback, final GetUrlCallback callback) {
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
            parseAppSearchResultPage(appToDownload, response, getAppDownloadUrlResponseCallback, callback);
          }
        }
      });
    } catch(Exception e) {
      log.error("Could not get Download Link for " + appToDownload, e);
      callback.completed(new GetUrlResponse(e.getLocalizedMessage()));
    }
  }

  protected void parseAppSearchResultPage(AppInfo appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback getAppDownloadUrlResponseCallback, GetUrlCallback callback) {
    Document document = Jsoup.parse(response.getBody());
    Elements tabContainerElements = document.body().select(".tab-container");

    if(tabContainerElements.size() > 0) {
      Element tabContainerElement = tabContainerElements.first();

      Elements appRowElements = tabContainerElement.parent().select(".appRow");

      if(appRowElements.size() > 0) {
        if(findAppDetails(appToDownload, document, appRowElements, getAppDownloadUrlResponseCallback, callback)) {
          return;
        }
      }
      else {
        boolean doesNotContainThisApp = checkIfDoesNotHaveThisApp(tabContainerElement);
        if(doesNotContainThisApp) {
          getAppDownloadUrlResponseCallback.completed(new GetAppDownloadUrlResponse(appToDownload, this, true));
          return;
        }
      }
    }

    callback.completed(new GetUrlResponse("Could not find App Details Page Url")); // TODO: translate
  }

  protected boolean findAppDetails(AppInfo appToDownload, Document document, Elements appRowElements, GetAppDownloadUrlResponseCallback getAppDownloadUrlResponseCallback, GetUrlCallback callback) {
    for(Element appRowElement : appRowElements) {
      String text = appRowElement.text();
      if(StringUtils.isNotNullOrEmpty(text) && text.contains("by ")) {
        String appTitleAndVersion = text.substring(0, text.lastIndexOf(" by")).trim();
        if(appTitleAndVersion.startsWith(appToDownload.getTitle())) {
          if(isCorrectApp(appToDownload, appTitleAndVersion)) {
            return extractAppDetailsPageUrl(appRowElement, callback);
          }
        }
      }
    }

    return checkIfHasMoreSearchResults(appToDownload, document, getAppDownloadUrlResponseCallback, callback);
  }

  protected boolean isCorrectApp(AppInfo appToDownload, String appTitleAndVersion) {
    boolean searchingForBetaVersion = appToDownload.getTitle().toLowerCase().contains("beta");

    String appVersion = appTitleAndVersion.substring(appToDownload.getTitle().length()).trim();
    boolean foundAppIsBeta = appVersion.toLowerCase().contains("beta");

    if(appToDownload.isVersionSet() == false) {
      return searchingForBetaVersion == foundAppIsBeta;
    }
    else {
      return appVersion.startsWith(appToDownload.getVersionString()) && searchingForBetaVersion == foundAppIsBeta;
    }
  }

  protected boolean extractAppDetailsPageUrl(Element appRowElement, GetUrlCallback callback) {
    Element downloadIconElement = appRowElement.select("a.fontBlack").first();

    if(downloadIconElement != null) {
      String appDetailsPageUrl = downloadIconElement.attr("href");
      appDetailsPageUrl = BASE_URL + appDetailsPageUrl;

      callback.completed(new GetUrlResponse(true, appDetailsPageUrl));
      return true;
    }

    return false;
  }

  protected boolean checkIfHasMoreSearchResults(AppInfo appToDownload, Document document, GetAppDownloadUrlResponseCallback getAppDownloadUrlResponseCallback, GetUrlCallback callback) {
    Element paginationElement = document.body().select("div.pagination").first();

    if(paginationElement != null) {
      Elements anchors = paginationElement.select("a");
      for(Element anchor : anchors) {
        if("Next >".equals(anchor.text().trim())) {
          getNextSearchResultsPage(appToDownload, anchor.attr("href"), getAppDownloadUrlResponseCallback, callback);
          return true;
        }
      }
    }

    return false;
  }

  protected void getNextSearchResultsPage(final AppInfo appToDownload, String nextSearchResultPageSubUrl, final GetAppDownloadUrlResponseCallback getAppDownloadUrlResponseCallback, final GetUrlCallback callback) {
    RequestParameters parameters = new RequestParameters(BASE_URL + nextSearchResultPageSubUrl);

    webClient.getAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetUrlResponse(response.getError()));
        }
        else {
          parseAppSearchResultPage(appToDownload, response, getAppDownloadUrlResponseCallback, callback);
        }
      }
    });
  }

  protected boolean checkIfDoesNotHaveThisApp(Element tabContainerElement) {
    Elements addPaddingElements = tabContainerElement.parent().select(".addpadding");
    if(addPaddingElements.size() > 0) {
      Element addPaddingElement = addPaddingElements.first();

      if(addPaddingElement.childNodeSize() > 0) {
        Element addPaddingElementChild = addPaddingElement.child(0);
        return "p".equals(addPaddingElementChild.nodeName()) && "No results found matching your query".equals(addPaddingElementChild.text());
      }
    }

    return false;
  }
}
