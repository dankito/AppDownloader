package net.dankito.appdownloader.app.apkverifier;

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
 * Created by ganymed on 22/11/16.
 */

public class AndroidObservatoryAkpSignatureVerifier implements IApkFileVerifier {

  protected static final String CHECK_APK_SIGNATURE_URL = "https://androidobservatory.org/?searchby=certhash&q=";

  private static final Logger log = LoggerFactory.getLogger(AndroidObservatoryAkpSignatureVerifier.class);


  protected IWebClient webClient;


  public AndroidObservatoryAkpSignatureVerifier(IWebClient webClient) {
    this.webClient = webClient;
  }


  @Override
  public void verifyApkFile(final DownloadedApkInfo downloadedApkInfo, final VerifyApkFileCallback callback) {
    log.info("Trying to verify Apk Signatures (" + downloadedApkInfo.getSignatureDigests().size() + ") for " + downloadedApkInfo + " ...");

    if(downloadedApkInfo.getSignatureDigests().size() < 1) {
      callback.completed(new VerifyApkFileResult(downloadedApkInfo, "No signatures available for apk file"));
    }
    else {
      // search for apps with this apk signature
      RequestParameters parameters = new RequestParameters(CHECK_APK_SIGNATURE_URL + downloadedApkInfo.getSignatureDigests().get(0)); // TODO: what about the others?

      webClient.getAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          if(response.isSuccessful() == false) {
            callback.completed(new VerifyApkFileResult(downloadedApkInfo, response.getError()));
          }
          else {
            parseApkSignatureResponse(downloadedApkInfo, response, callback);
          }
        }
      });
    }
  }


  protected void parseApkSignatureResponse(DownloadedApkInfo downloadedApkInfo, WebClientResponse response, VerifyApkFileCallback callback) {
    Document document = Jsoup.parse(response.getBody());
    Element tableElement = document.body().select("table").first(); // table with search results for this signature. if it's available, AndroidObservatory knows this signature

    boolean signatureIsValidForThisApp = false;

    if(tableElement != null) {
      Elements tableDataElements = tableElement.select("tbody td");
      for(Element tableDataElement : tableDataElements) {
        String cellText = tableDataElement.text();
        if(cellText != null && (cellText.contains(downloadedApkInfo.getApp().getTitle()) || cellText.contains(downloadedApkInfo.getPackageName()))) {
          signatureIsValidForThisApp = true;
          break;
        }
      }
    }

    VerifyApkFileResult result = new VerifyApkFileResult(downloadedApkInfo, signatureIsValidForThisApp, signatureIsValidForThisApp);
    log.info("Does AndroidObservatory know Signature for this App? " + signatureIsValidForThisApp);

    callback.completed(result);
  }

}
