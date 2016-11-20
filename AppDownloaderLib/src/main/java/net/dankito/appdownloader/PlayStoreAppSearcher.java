package net.dankito.appdownloader;

import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.IAppDetailsCache;
import net.dankito.appdownloader.app.IInstalledAppsManager;
import net.dankito.appdownloader.responses.GetAppDetailsResponse;
import net.dankito.appdownloader.responses.SearchAppsResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDetailsCallback;
import net.dankito.appdownloader.responses.callbacks.SearchAppsResponseCallback;
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ganymed on 02/11/16.
 */

public class PlayStoreAppSearcher implements IPlayStoreAppSearcher {

  protected static final String BASE_URL = "https://play.google.com";

  protected static final int MAX_SEARCH_RESULT_PAGE_NUMBER = 4;

  protected static final int COUNT_SEARCH_RESULTS_PER_PAGE = 20;

  protected static final String PACKAGE_NAME_ATTRIBUTE_NAME = "data-docid";

  protected static final int CONNECTION_TIMEOUT_MILLIS = 2000;
  protected static final int COUNT_CONNECTION_RETRIES = 2;

  private static final Logger log = LoggerFactory.getLogger(PlayStoreAppSearcher.class);


  protected IWebClient webClient;

  protected IInstalledAppsManager installedAppsManager;

  protected IAppDetailsCache appDetailsCache;

  protected List<GetAppDetailsCallback> appDetailsListeners = new ArrayList<>();


  @Inject
  public PlayStoreAppSearcher(IWebClient webClient, IInstalledAppsManager installedAppsManager, IAppDetailsCache appDetailsCache) {
    this.webClient = webClient;
    this.installedAppsManager = installedAppsManager;
    this.appDetailsCache = appDetailsCache;
  }


  @Override
  public void searchAsync(String searchTerm, final SearchAppsResponseCallback callback) {
    try {
      final String searchUrl = "https://play.google.com/store/search?q=" + URLEncoder.encode(searchTerm, "ASCII") + "&c=apps&docType=1";
      searchAsync(searchUrl, 0, callback);
    } catch(Exception e) {
      log.error("Could not search for '" + searchTerm + "'", e);
      callback.completed(new SearchAppsResponse(e.getLocalizedMessage()));
    }
  }

  protected void searchAsync(final String searchUrl, final int pageNumber, final SearchAppsResponseCallback callback) {
    RequestParameters parameters = createRequestParametersWithDefaultValues(searchUrl);

    webClient.postAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new SearchAppsResponse(response.getError()));
        }
        else {
          parseSearchAppsResponse(searchUrl, pageNumber, response, callback);
        }
      }
    });
  }

  protected void parseSearchAppsResponse(String searchUrl, int pageNumber, WebClientResponse response, SearchAppsResponseCallback callback) {
    parseSearchAppsResponse(searchUrl, pageNumber, response, new ArrayList<AppInfo>(), callback);
  }

  protected void parseSearchAppsResponse(String searchUrl, int pageNumber, WebClientResponse response, List<AppInfo> searchResults, SearchAppsResponseCallback callback) {
    try {
      String responseBody = response.getBody();

      Document document = Jsoup.parse(responseBody);
      // card-lists contain 'cards', one for each result App (Book etc.)
      Elements cardListElements = document.body().select(".id-card-list"); // don't do too much queries, is very slow on Android

      for(Element cardList : cardListElements) {
        for(Element cardListChild : cardList.children()) {
          if(cardListChild.hasClass("card") && cardListChild.hasClass("apps")) { // TODO: implement Option to not only filter Apps
            AppInfo app = parseCardElement(cardListChild);
            if(app != null) {
              addAppInstallationInfo(app);
              searchResults.add(app);
              setAppDetails(app);
            }
          }
        }
      }

      pageNumber++;
      if(pageNumber < MAX_SEARCH_RESULT_PAGE_NUMBER && searchResults.size() == pageNumber * COUNT_SEARCH_RESULTS_PER_PAGE) {
        retrieveAndParseNextSearchResultPage(searchUrl, pageNumber, responseBody, document, searchResults, callback);
      }
      else {
        callback.completed(new SearchAppsResponse(searchResults));
      }
    } catch(Exception e) {
      log.error("Could not parse Apps Search Response", e);
      callback.completed(new SearchAppsResponse(e.getLocalizedMessage()));
    }
  }

  protected AppInfo parseCardElement(Element cardElement) {
    for(Element cardElementChild : cardElement.children()) {
      if(cardElementChild.hasClass("card-content")) {
        return parseCardContentElement(cardElementChild);
      }
    }

    return null;
  }

  protected AppInfo parseCardContentElement(Element cardContentElement) {
    String packageName = cardContentElement.attr(PACKAGE_NAME_ATTRIBUTE_NAME);

    AppInfo appInfo = new AppInfo(packageName);

    for(Element child : cardContentElement.children()) {
      if("div".equals(child.nodeName())) {
        if(child.hasClass("details")) {
          parseDetails(appInfo, child);
        }
        else if(child.hasClass("cover")) {
          parseCover(appInfo, child);
        }
      }
    }

    if(appInfo.areNecessaryInformationSet()) {
      return appInfo;
    }
    return null;
  }

  protected void parseDetails(AppInfo appInfo, Element cardDetailsElement) {
    for(Element child : cardDetailsElement.children()) {
      if("a".equals(child.nodeName()) && child.hasClass("title")) {
        appInfo.setAppDetailsPageUrl(BASE_URL + child.attr("href"));
        appInfo.setTitle(child.attr("title"));
      }
      else if("div".equals(child.nodeName()) && child.hasClass("subtitle-container")) {
        for(Element containerChild : child.children()) {
          if("a".equals(containerChild.nodeName()) && containerChild.hasClass("subtitle")) {
            appInfo.setDeveloper(containerChild.text());
          }
        }
      }
    }
  }

  protected void parseCover(AppInfo appInfo, Element coverElement) {
    Elements coverImageElements = coverElement.select(".cover-image"); // TODO: don't use queries, they are very slow on Android
    if(coverImageElements.size() == 1) {
      Element coverImageElement = coverImageElements.get(0);

      appInfo.setSmallCoverImageUrl("http:" + coverImageElement.attr("data-cover-small"));
      appInfo.setLargeCoverImageUrl("http:" + coverImageElement.attr("data-cover-large"));
    }
  }


  protected void retrieveAndParseNextSearchResultPage(String searchUrl, int pageNumber, String responseBody, Document document, List<AppInfo> searchResults, SearchAppsResponseCallback callback) {
    try {
      Element seeMoreElement = document.body().select("a.see-more[data-server-cookie").first();
      String nextSearchResultsPageReferer = seeMoreElement.attr("href");

      String token = extractTokenParameter(responseBody);

      String sp = extractSpParameter(nextSearchResultsPageReferer);

      retrieveNextSearchResultPage(searchUrl, nextSearchResultsPageReferer, token, sp, pageNumber, searchResults, callback);
    } catch(Exception e) {
      log.error("Could not retrieve next search result page", e);
      callback.completed(new SearchAppsResponse(searchResults));
    }
  }

  protected String extractTokenParameter(String responseBody) throws UnsupportedEncodingException {
    int nbpVariableStartIndex = responseBody.indexOf("var nbp='[") + "var nbp='[".length();
    int nbpVariableEndIndex = responseBody.indexOf(']', nbpVariableStartIndex);
    String nbpJavaScriptArray = responseBody.substring(nbpVariableStartIndex, nbpVariableEndIndex);

    String[] arrayFields = nbpJavaScriptArray.split(",");
    String token = null;

    if(arrayFields.length > 1) {
      token = arrayFields[1];
      token = token.replace("\\x22", "");
      token = URLEncoder.encode(token, "ASCII");
    }
    return token;
  }

  protected String extractSpParameter(String nextSearchResultsPageUrl) {
    int spStartIndex = nextSearchResultsPageUrl.indexOf("&sp=") + "&sp=".length();
    int spEndIndex = nextSearchResultsPageUrl.indexOf("&", spStartIndex);
    String sp = nextSearchResultsPageUrl.substring(spStartIndex);
    if (spEndIndex > 0) {
      sp = nextSearchResultsPageUrl.substring(spStartIndex, spEndIndex);
    }
//      sp = URLEncoder.encode(sp, "ASCII");
    sp = sp.replace(":", "%3A");
    return sp;
  }

  protected void retrieveNextSearchResultPage(String searchUrl, String nextSearchResultsPageReferer, String token, String sp, int pageNumber, List<AppInfo> searchResults, SearchAppsResponseCallback callback) {
    RequestParameters nextSearchResultsParameters = new RequestParameters(searchUrl);
    nextSearchResultsParameters.addHeader("Referer", nextSearchResultsPageReferer);
    nextSearchResultsParameters.addHeader("Accept-Language", "en-US,en;q=0.8");

    String requestBody = "start=0&num=0&numChildren=0&pagTok=" + token + "&sp=" + sp + "&cctcss=square-cover&cllayout=NORMAL&ipf=1&xhr=1";
    nextSearchResultsParameters.setBody(requestBody);

    WebClientResponse nextSearchResultResponse = webClient.post(nextSearchResultsParameters);
    if(nextSearchResultResponse.isSuccessful()) {
      parseSearchAppsResponse(searchUrl, pageNumber, nextSearchResultResponse, searchResults, callback);
    }
    else {
      callback.completed(new SearchAppsResponse(searchResults));
    }
  }


  protected void addAppInstallationInfo(AppInfo app) {
    AppInfo installedAppInfo = installedAppsManager.getAppInstallationInfo(app.getPackageName());

    if(installedAppInfo == null) {
      app.setAlreadyInstalled(false);
    }
    else {
      app.setIconImage(installedAppInfo.getIconImage());
      app.setAlreadyInstalled(true);
      app.setInstalledVersionString(installedAppInfo.getInstalledVersionString());
    }
  }

  protected void setAppDetails(AppInfo appInfo) {
    if(appDetailsCache.hasAppDetailsRetrievedForApp(appInfo)) {
      appDetailsCache.setAppDetailsForApp(appInfo);
    }
    else {
      getAppDetailsAsync(appInfo);
    }
  }

  protected void getAppDetailsAsync(AppInfo appInfo) {
    getAppDetailsAsync(appInfo, new GetAppDetailsCallback() {
      @Override
      public void completed(GetAppDetailsResponse response) {
        if(response.isSuccessful()) {
          for(GetAppDetailsCallback listener : appDetailsListeners) {
            listener.completed(response);
          }
        }
      }
    });
  }

  @Override
  public void getAppDetailsAsync(final AppInfo appInfo, final GetAppDetailsCallback callback) {
    try {
      RequestParameters parameters = new RequestParameters(appInfo.getAppDetailsPageUrl());
      parameters.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MILLIS);
      parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);

      webClient.getAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new GetAppDetailsResponse(appInfo, response.getError()));
          }
          else {
            getAppDetailsCompleted(appInfo, response, callback);
          }
        }
      });
    } catch(Exception e) {
      log.error("Could not get App Detail Page for App " + appInfo, e);
      callback.completed(new GetAppDetailsResponse(appInfo, e.getLocalizedMessage()));
    }
  }

  protected void getAppDetailsCompleted(AppInfo appInfo, WebClientResponse response, GetAppDetailsCallback callback) {
    try {
      String appDetailsPageHtml = response.getBody();

      parseAppDetailsPage(appInfo, appDetailsPageHtml);

      appDetailsCache.cacheAppDetails(appInfo);

      callback.completed(new GetAppDetailsResponse(appInfo, appInfo.areAppDetailsDownloaded()));
    } catch(Exception e) {
      log.error("Could not get App Detail Page for App " + appInfo, e);
      callback.completed(new GetAppDetailsResponse(appInfo, e.getLocalizedMessage()));
    }
  }

  protected void parseAppDetailsPage(AppInfo appInfo, String appDetailsPageHtml) {
    Document document = Jsoup.parse(appDetailsPageHtml);

    parseAppDetailsSection(appInfo, document);

    parseScoreContainer(appInfo, document);
  }

  protected void parseAppDetailsSection(AppInfo appInfo, Document document) {
    Elements reportElements = document.body().select("a[href='https://support.google.com/googleplay/?p=report_content']");
    if(reportElements.size() > 0) {
      Element detailsSectionElement = reportElements.get(0).parent().parent();
      parseAppDetailsSection(appInfo, detailsSectionElement);
    }
  }

  protected void parseAppDetailsSection(AppInfo appInfo, Element detailsSectionElement) {
    for(Element child : detailsSectionElement.children()) {
      if("div".equals(child.nodeName()) && child.hasClass("meta-info")) {
        parseAppDetailsInfo(appInfo, child);
      }
    }
  }

  protected void parseAppDetailsInfo(AppInfo appInfo, Element detailInfoElement) {
    for(Element child : detailInfoElement.children()) {
      if("div".equals(child.nodeName()) && child.hasAttr("itemprop")) {
        String detailName = child.attr("itemprop");

        if("numDownloads".equals(detailName)) {
          appInfo.setCountInstallations(child.text());
        }
        else if("softwareVersion".equals(detailName)) {
          appInfo.setVersionString(child.text());
        }
      }
    }
  }

  protected void parseScoreContainer(AppInfo appInfo, Document document) {
    Elements scoreContainerElements = document.body().select(".score-container");
    if(scoreContainerElements.size() > 0) {
      Element scoreContainerElement = scoreContainerElements.get(0);
      parseScoreContainer(appInfo, scoreContainerElement);
    }
  }

  protected void parseScoreContainer(AppInfo appInfo, Element scoreContainerElement) {
    for(Element child : scoreContainerElement.children()) {
      if(child.hasClass("score")) {
        appInfo.setRating(child.text());
      }
      else if(child.hasClass("reviews-stats")) {
        for(Element reviewStatsChild : child.children()) {
          if(reviewStatsChild.hasClass("reviews-num")) {
            appInfo.setCountRatings(reviewStatsChild.text());
            break;
          }
        }
      }
    }
  }


  protected RequestParameters createRequestParametersWithDefaultValues(String url) {
    RequestParameters parameters = new RequestParameters(url);

    parameters.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MILLIS);
    parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);

    return parameters;
  }


  @Override
  public boolean addRetrievedAppDetailsListener(GetAppDetailsCallback listener) {
    return appDetailsListeners.add(listener);
  }

  @Override
  public boolean removeRetrievedAppDetailsListener(GetAppDetailsCallback listener) {
    return appDetailsListeners.remove(listener);
  }

}
