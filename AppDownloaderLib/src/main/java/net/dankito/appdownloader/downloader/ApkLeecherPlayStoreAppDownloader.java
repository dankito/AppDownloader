package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppDownloadInfo;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.HashAlgorithm;
import net.dankito.appdownloader.downloader.util.ApkLeecherDownloadLinkGenerator;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestCallback;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 20/11/16.
 */

public class ApkLeecherPlayStoreAppDownloader extends AppDownloaderBase {

  protected static final String SEARCH_URL_BASE = "http://apkleecher.com/?id=";

  private static final Logger log = LoggerFactory.getLogger(ApkLeecherPlayStoreAppDownloader.class);


  public ApkLeecherPlayStoreAppDownloader(IWebClient webClient) {
    super(webClient);
  }


  @Override
  public void getAppDownloadLinkAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback) {
    String searchUrl = SEARCH_URL_BASE + appToDownload.getPackageName();
    RequestParameters parameters = createRequestParametersWithDefaultValues(searchUrl);

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

  private void parseAppDetailsPage(AppInfo appToDownload, WebClientResponse response, GetAppDownloadUrlResponseCallback callback) {
    try {
      Document document = Jsoup.parse(response.getBody());

      AppDownloadInfo downloadInfo = parseAppDetails(appToDownload, document);
      appToDownload.addDownloadUrl(downloadInfo);

      if(downloadInfo.isHasDownloadLink()) {
        callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, downloadInfo));
      }
      else {
        callback.completed(new GetAppDownloadUrlResponse(appToDownload, true));
      }
    } catch(Exception e) {
      log.error("Could not parse App Details Page", e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

  protected AppDownloadInfo parseAppDetails(AppInfo appToDownload, Document document) {
    AppDownloadInfo downloadInfo = new AppDownloadInfo(appToDownload, this);
    ApkLeecherDownloadLinkGenerator downloadLinkGenerator = new ApkLeecherDownloadLinkGenerator();

    Element detailsElement = document.body().select("p.text-success").first();
    if(detailsElement != null) {
      for(Element childNode : detailsElement.children()) {
        if("strong".equals(childNode.nodeName())) {
          parseAppDetails(downloadInfo, downloadLinkGenerator, childNode);
        }
      }
    }

    String downloadUrl = downloadLinkGenerator.generateDownloadUrl();
    if(downloadUrl != null) {
      downloadInfo.setUrl(downloadUrl);
    }

    return downloadInfo;
  }



  protected void parseAppDetails(AppDownloadInfo downloadInfo, ApkLeecherDownloadLinkGenerator downloadLinkGenerator, Element appDetailElement) {
    String appDetailName = appDetailElement.childNode(0).toString().trim();
    String appDetailValue = appDetailElement.nextSibling().toString().trim();

    // AppDownloadInfo
    if("File Size:".equals(appDetailName)) {
      downloadInfo.setFileSize(appDetailValue);
    }
    else if("MD5 File Hash:".equals(appDetailName)) {
      downloadInfo.setHashAlgorithm(HashAlgorithm.MD5);
      downloadInfo.setFileHashSum(appDetailValue);
    }

    // DownloadLinkGenerator
    else if("App Name:".equals(appDetailName)) {
      Element nextDetailElement = appDetailElement.nextElementSibling();
      nextDetailElement = nextDetailElement.children().first() == null ? nextDetailElement : nextDetailElement.children().first();
      downloadLinkGenerator.setAppNameAndVersion(nextDetailElement.text());
    }
    else if("Updated:".equals(appDetailName)) {
      downloadLinkGenerator.setLastUpdatedOn(appDetailValue);
    }
  }
}
