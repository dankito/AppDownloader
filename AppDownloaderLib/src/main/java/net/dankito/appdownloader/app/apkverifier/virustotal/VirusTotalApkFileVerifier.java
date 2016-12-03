package net.dankito.appdownloader.app.apkverifier.virustotal;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.appdownloader.app.apkverifier.IApkFileVerifier;
import net.dankito.appdownloader.app.apkverifier.DownloadedApkInfo;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanReportResponse;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileResult;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileCallback;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ganymed on 22/11/16.
 */

public class VirusTotalApkFileVerifier implements IApkFileVerifier {

  private static final String VIRUS_TOTAL_PROPERTIES_FILENAME = "virus_total.properties";

  private static final String VIRUS_TOTAL_API_KEY_PROPERTY_NAME = "api.key";

  public static final int MAX_NUMBER_OF_POSITIVE_FILE_SCANS_TO_JUDGE_SECURE = 2;


  private static final String API_KEY_FIELD = "apikey";

  private static final String RESOURCE_FIELD = "resource";


  private static final Logger log = LoggerFactory.getLogger(VirusTotalApkFileVerifier.class);


  protected IWebClient webClient;

  protected String apiKey = null;

  protected ObjectMapper mapper = new ObjectMapper();


  public VirusTotalApkFileVerifier(IWebClient webClient) {
    this.webClient = webClient;
  }


  public void verifyApkFile(DownloadedApkInfo downloadedApkInfo, VerifyApkFileCallback callback) {
    try {
      String url = "https://www.virustotal.com/vtapi/v2/file/report";
      String body = API_KEY_FIELD + "=" + getApiKey() + "&" + RESOURCE_FIELD + "=" + downloadedApkInfo.getSha256CheckSum();
      RequestParameters parameters = new RequestParameters(url, body);

      WebClientResponse response = webClient.post(parameters);
      if(response.isSuccessful() == false) {
        callback.completed(createRequestFailedResult(downloadedApkInfo, response));
      }
      else {
        parseGetFileReportResponse(downloadedApkInfo, response, callback);
      }
    } catch(Exception e) {
      log.error("Could not verify downloaded Apk file " + downloadedApkInfo.getApp(), e);
      callback.completed(new VerifyApkFileResult(downloadedApkInfo, "Could not verify downloaded Apk file " + downloadedApkInfo.getApp() + ": " + e.getLocalizedMessage()));
    }
  }

  protected void parseGetFileReportResponse(DownloadedApkInfo downloadedApkInfo, WebClientResponse response, VerifyApkFileCallback callback) throws IOException {
    String responseBody = response.getBody();
    VirusTotalFileScanReportResponse fileScanReportResponse = mapper.readValue(responseBody, VirusTotalFileScanReportResponse.class);

    boolean knowsApkFile = fileScanReportResponse.getResponse_code() == ResponseCode.ITEM_FOUND.getCode() &&
                           fileScanReportResponse.getTotal() > 0; // response successful and at least one file scan
    boolean isSecure = fileScanReportResponse.getPositives() < MAX_NUMBER_OF_POSITIVE_FILE_SCANS_TO_JUDGE_SECURE;

    VerifyApkFileResult result = new VerifyApkFileResult(downloadedApkInfo, knowsApkFile, isSecure, true);
    callback.completed(result);
  }



  protected VerifyApkFileResult createRequestFailedResult(DownloadedApkInfo downloadedApkInfo, WebClientResponse response) {
    // TODO: check if response code is 403 (HTTP_FORBIDDEN) or 204 (API_LIMIT_EXCEEDED) and tell user, that api key is incorrect or API limit exceeded
    return new VerifyApkFileResult(downloadedApkInfo, response.getError());
  }


  protected String getApiKey() throws Exception {
    if(apiKey == null) {
      apiKey = readApiKey();
    }

    return apiKey;
  }

  protected String readApiKey() throws Exception {
    InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream(VIRUS_TOTAL_PROPERTIES_FILENAME);

    Properties virusTotalProperties = new Properties();
    virusTotalProperties.load(propertiesInputStream);

    propertiesInputStream.close();

    return virusTotalProperties.getProperty(VIRUS_TOTAL_API_KEY_PROPERTY_NAME);
  }

}
