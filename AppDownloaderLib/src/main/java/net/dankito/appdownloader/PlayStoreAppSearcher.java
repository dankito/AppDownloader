package net.dankito.appdownloader;

import net.dankito.appdownloader.responses.AppSearchResult;
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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Created by ganymed on 02/11/16.
 */

public class PlayStoreAppSearcher {

  protected static final String GET_APP_DETAIL_PAGE_URL = "https://play.google.com/store/apps/details?id=";

  protected static final String PACKAGE_NAME_ATTRIBUTE_NAME = "data-docid";
  protected static final String COOKIE_ATTRIBUTE_NAME = "data-server-cookie";

  protected static final int CONNECTION_TIMEOUT_MILLIS = 2000;
  protected static final int COUNT_CONNECTION_RETRIES = 2;

  private static final Logger log = LoggerFactory.getLogger(PlayStoreAppSearcher.class);


  protected IWebClient webClient;

  protected List<GetAppDetailsCallback> appDetailsListeners = new ArrayList<>();


  @Inject
  public PlayStoreAppSearcher(IWebClient webClient) {
    this.webClient = webClient;
  }


  public void searchAsync(String searchTerm, final SearchAppsResponseCallback callback) {
    try {
      String searchUrl = "https://play.google.com/store/search?q=" + URLEncoder.encode(searchTerm, "ASCII") + "&authuser=0";
      RequestParameters parameters = new RequestParameters(searchUrl, "ipf=1&xhr=1");
      parameters.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MILLIS);
      parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);

      webClient.postAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new SearchAppsResponse(response.getError()));
          }
          else {
            parseSearchAppsResponse(response, callback);
          }
        }
      });
    } catch(Exception e) {
      log.error("Could not search for '" + searchTerm + "'", e);
      callback.completed(new SearchAppsResponse(e.getLocalizedMessage()));
    }
  }

  protected void parseSearchAppsResponse(WebClientResponse response, SearchAppsResponseCallback callback) {
    try {
      List<AppSearchResult> appSearchResults = new ArrayList<>();

      String responseBody = response.getBody();

      Document document = Jsoup.parse(responseBody);
      // card-lists contain 'cards', one for each result App (Book etc.)
      Elements cardListElements = document.body().select(".id-card-list"); // don't do too much queries, is very slow on Android

      for(Element cardList : cardListElements) {
        for(Element cardListChild : cardList.children()) {
          if(cardListChild.hasClass("card") && cardListChild.hasClass("apps")) { // TODO: implement Option to not only filter Apps
            AppSearchResult app = parseCardElement(cardListChild);
            if(app != null) {
              appSearchResults.add(app);
              getAppDetailsAsync(app);
            }
          }
        }
      }

      callback.completed(new SearchAppsResponse(appSearchResults));
    } catch(Exception e) {
      log.error("Could not parse Apps Search Response", e);
      callback.completed(new SearchAppsResponse(e.getLocalizedMessage()));
    }
  }

  protected AppSearchResult parseCardElement(Element cardElement) {
    for(Element cardElementChild : cardElement.children()) {
      if(cardElementChild.hasClass("card-content")) {
        return parseCardContentElement(cardElementChild);
      }
    }

    return null;
  }

  protected AppSearchResult parseCardContentElement(Element cardContentElement) {
    String packageName = cardContentElement.attr(PACKAGE_NAME_ATTRIBUTE_NAME);
    String cookie = cardContentElement.attr(COOKIE_ATTRIBUTE_NAME); // TODO: do we need this?

    AppSearchResult appSearchResult = new AppSearchResult(packageName);

    for(Element child : cardContentElement.children()) {
      if("div".equals(child.nodeName())) {
        if(child.hasClass("details")) {
          parseDetails(appSearchResult, child);
        }
        else if(child.hasClass("cover")) {
          parseCover(appSearchResult, child);
        }
      }
    }

    if(appSearchResult.areNecessaryInformationSet()) {
      return appSearchResult;
    }
    return null;
  }

  protected void parseDetails(AppSearchResult appSearchResult, Element cardDetailsElement) {
    for(Element child : cardDetailsElement.children()) {
      if("a".equals(child.nodeName()) && child.hasClass("title")) {
        appSearchResult.setAppUrl(child.attr("href"));
        appSearchResult.setTitle(child.attr("title"));
      }
      else if("div".equals(child.nodeName()) && child.hasClass("subtitle-container")) {
        for(Element containerChild : child.children()) {
          if("a".equals(containerChild.nodeName()) && containerChild.hasClass("subtitle")) {
            appSearchResult.setDeveloper(containerChild.text());
          }
        }
      }
    }
  }

  protected void parseCover(AppSearchResult appSearchResult, Element coverElement) {
    Elements coverImageElements = coverElement.select(".cover-image"); // TODO: don't use queries, they are very slow on Android
    if(coverImageElements.size() == 1) {
      Element coverImageElement = coverImageElements.get(0);

      appSearchResult.setSmallCoverImageUrl("http:" + coverImageElement.attr("data-cover-small"));
      appSearchResult.setLargeCoverImageUrl("http:" + coverImageElement.attr("data-cover-large"));
    }
  }


  protected void getAppDetailsAsync(AppSearchResult appSearchResult) {
    getAppDetailsAsync(appSearchResult, new GetAppDetailsCallback() {
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

  public void getAppDetailsAsync(final AppSearchResult appSearchResult, final GetAppDetailsCallback callback) {
    try {
      RequestParameters parameters = new RequestParameters(appSearchResult.getAppDetailsPageUrl());
      parameters.setConnectionTimeoutMillis(CONNECTION_TIMEOUT_MILLIS);
      parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);

      webClient.getAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          getAppDetailsCompleted(appSearchResult, response, callback);
        }
      });
    } catch(Exception e) {
      log.error("Could not get App Detail Page for App " + appSearchResult, e);
      callback.completed(new GetAppDetailsResponse(appSearchResult, e.getLocalizedMessage()));
    }
  }

  protected void getAppDetailsCompleted(AppSearchResult appSearchResult, WebClientResponse response, GetAppDetailsCallback callback) {
    try {
      String appDetailsPageHtml = response.getBody();
      appSearchResult.setAppDetailsPageHtml(appDetailsPageHtml);

      parseAppDetailsPage(appSearchResult, appDetailsPageHtml);

      callback.completed(new GetAppDetailsResponse(appSearchResult, appSearchResult.areAppDetailsDownloaded()));
    } catch(Exception e) {
      log.error("Could not get App Detail Page for App " + appSearchResult, e);
      callback.completed(new GetAppDetailsResponse(appSearchResult, e.getLocalizedMessage()));
    }
  }

  protected void parseAppDetailsPage(AppSearchResult appSearchResult, String appDetailsPageHtml) {
    Document document = Jsoup.parse(appDetailsPageHtml);

    parseAppDetailsSection(appSearchResult, document);

    parseScoreContainer(appSearchResult, document);
  }

  protected void parseAppDetailsSection(AppSearchResult appSearchResult, Document document) {
    Elements reportElements = document.body().select("a[href='https://support.google.com/googleplay/?p=report_content']");
    if(reportElements.size() > 0) {
      Element detailsSectionElement = reportElements.get(0).parent().parent();
      parseAppDetailsSection(appSearchResult, detailsSectionElement);
    }
  }

  protected void parseAppDetailsSection(AppSearchResult appSearchResult, Element detailsSectionElement) {
    for(Element child : detailsSectionElement.children()) {
      if("div".equals(child.nodeName()) && child.hasClass("meta-info")) {
        parseAppDetailsInfo(appSearchResult, child);
      }
    }
  }

  protected void parseAppDetailsInfo(AppSearchResult appSearchResult, Element detailInfoElement) {
    for(Element child : detailInfoElement.children()) {
      if("div".equals(child.nodeName()) && child.hasAttr("itemprop")) {
        String detailName = child.attr("itemprop");

        if("numDownloads".equals(detailName)) {
          appSearchResult.setCountInstallations(child.text());
        }
        else if("softwareVersion".equals(detailName)) {
          appSearchResult.setVersion(child.text());
        }
      }
    }
  }

  protected void parseScoreContainer(AppSearchResult appSearchResult, Document document) {
    Elements scoreContainerElements = document.body().select(".score-container");
    if(scoreContainerElements.size() > 0) {
      Element scoreContainerElement = scoreContainerElements.get(0);
      parseScoreContainer(appSearchResult, scoreContainerElement);
    }
  }

  protected void parseScoreContainer(AppSearchResult appSearchResult, Element scoreContainerElement) {
    for(Element child : scoreContainerElement.children()) {
      if(child.hasClass("score")) {
        appSearchResult.setRating(child.text());
      }
      else if(child.hasClass("reviews-stats")) {
        for(Element reviewStatsChild : child.children()) {
          if(reviewStatsChild.hasClass("reviews-num")) {
            appSearchResult.setCountRatings(reviewStatsChild.text());
            break;
          }
        }
      }
    }
  }


  public boolean addRetrievedAppDetailsListener(GetAppDetailsCallback listener) {
    return appDetailsListeners.add(listener);
  }

  public boolean removeRetrievedAppDetailsListener(GetAppDetailsCallback listener) {
    return appDetailsListeners.remove(listener);
  }

}
