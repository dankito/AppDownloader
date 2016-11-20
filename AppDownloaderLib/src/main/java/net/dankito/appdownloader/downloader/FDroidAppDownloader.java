package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppDownloadInfo;
import net.dankito.appdownloader.app.AppInfo;
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

/**
 * Created by ganymed on 20/11/16.
 */

public class FDroidAppDownloader extends AppDownloaderBase {

  protected static final String APP_DETAILS_PAGE_URL_PREFIX = "https://f-droid.org/repository/browse/?fdfilter=";

  protected static final String APP_DETAILS_PAGE_URL_CENTER_PART = "&fdid=";

  private static final Logger log = LoggerFactory.getLogger(FDroidAppDownloader.class);


  public FDroidAppDownloader(IWebClient webClient) {
    super(webClient);
  }


  @Override
  public void getAppDownloadLinkAsync(final AppInfo appToDownload, final GetAppDownloadUrlResponseCallback callback) {
    String url = APP_DETAILS_PAGE_URL_PREFIX + appToDownload.getPackageName() + APP_DETAILS_PAGE_URL_CENTER_PART + appToDownload.getPackageName();
    RequestParameters parameters = new RequestParameters(url);

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
    try {
      Document document = Jsoup.parse(response.getBody());
      Elements anchorElements = document.select("a");

      for(Element anchorElement : anchorElements) {
        if("download apk".equals(anchorElement.text().trim())) {
          AppDownloadInfo downloadInfo = new AppDownloadInfo(appToDownload, this);
          downloadInfo.setUrl(anchorElement.attr("href"));

          appToDownload.addDownloadUrl(downloadInfo);

          callback.completed(new GetAppDownloadUrlResponse(true, appToDownload, downloadInfo));
          return;
        }
      }

      callback.completed(new GetAppDownloadUrlResponse(appToDownload, true));
    } catch(Exception e) {
      log.error("Could not parse App Detail Page", e);
      callback.completed(new GetAppDownloadUrlResponse(appToDownload, e.getLocalizedMessage()));
    }
  }

}
