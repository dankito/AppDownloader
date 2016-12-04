package net.dankito.appdownloader.app.apkverifier.virustotal;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.appdownloader.app.apkverifier.virustotal.calback.GetFileScanReportCallback;
import net.dankito.appdownloader.app.apkverifier.virustotal.calback.ScanFileCallback;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.GetFileScanReportResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.ScanFileResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanReportResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanResponse;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestCallback;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;
import net.dankito.appdownloader.util.web.model.FileRequestBodyPart;
import net.dankito.appdownloader.util.web.model.StringRequestBodyPart;

import java.io.File;

/**
 * Documentation for the public VirusTotal API V2 can be found here: https://www.virustotal.com/en/documentation/public-api/
 */
public class VirusTotalApiV2Client {

  public static final String BASE_URL = "https://www.virustotal.com/vtapi/v2/";

  public static final String FILE_SCAN_SUB_URL = "file/scan";

  public static final String FILE_SCAN_REPORT_SUB_URL = "file/report";


  private static final String API_KEY_FIELD = "apikey";

  private static final String RESOURCE_FIELD = "resource";

  private static final String FILE_FIELD = "file";

  private static final String SCAN_ID_FIELD = "scan_id";


  protected String apiKey;

  protected IWebClient webClient;

  protected ObjectMapper mapper = new ObjectMapper();


  public VirusTotalApiV2Client(String apiKey, IWebClient webClient) {
    this.apiKey = apiKey;
    this.webClient = webClient;
  }


  public void getFileScanReportAsync(final FileScanRequest request, final GetFileScanReportCallback callback) {
    RequestParameters parameters = new RequestParameters(BASE_URL + FILE_SCAN_REPORT_SUB_URL);

    parameters.addRequestBodyPart(new StringRequestBodyPart(API_KEY_FIELD, apiKey));
    parameters.addRequestBodyPart(new StringRequestBodyPart(RESOURCE_FIELD, request.getSha256Checksum()));
    if(request.isScanIdSet()) {
      parameters.addRequestBodyPart(new StringRequestBodyPart(SCAN_ID_FIELD, request.getScanId()));
    }

    webClient.postAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new GetFileScanReportResponse(getErrorCode(response), response.getError()));
        }
        else {
          parseGetFileScanReportResponse(response, callback);
        }
      }
    });
  }

  protected void parseGetFileScanReportResponse(WebClientResponse webClientResponse, GetFileScanReportCallback callback) {
    try {
      String responseBody = webClientResponse.getBody();
      VirusTotalFileScanReportResponse fileScanReportResponse = mapper.readValue(responseBody, VirusTotalFileScanReportResponse.class);

      ResponseCode responseCode = ResponseCode.fromCode(fileScanReportResponse.getResponse_code());
      GetFileScanReportResponse response = new GetFileScanReportResponse(responseCode, fileScanReportResponse);

      callback.completed(response);
    } catch(Exception e) {
      callback.completed(new GetFileScanReportResponse(ResponseCode.PARSE_ERROR, e.getLocalizedMessage()));
    }
  }

  public void scanFileAsync(final FileScanRequest request, final ScanFileCallback callback) {
    String url = BASE_URL + FILE_SCAN_SUB_URL;
    RequestParameters parameters = new RequestParameters(url);
    parameters.addRequestBodyPart(new StringRequestBodyPart(API_KEY_FIELD, apiKey));
    parameters.addRequestBodyPart(new FileRequestBodyPart(FILE_FIELD, new File(request.getFilePath()), request.getFileMimeType()));

    webClient.postAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new ScanFileResponse(getErrorCode(response), response.getError()));
        }
        else {
          parseScanFileResponse(response, callback);
        }
      }
    });
  }

  protected void parseScanFileResponse(WebClientResponse webClientResponse, ScanFileCallback callback) {
    try {
      String responseBody = webClientResponse.getBody();
      VirusTotalFileScanResponse scanFileResponse = mapper.readValue(responseBody, VirusTotalFileScanResponse.class);

      ResponseCode responseCode = ResponseCode.fromCode(scanFileResponse.getResponse_code());
      boolean isSuccessfullyQueued = responseCode == ResponseCode.ITEM_FOUND && scanFileResponse.getVerbose_msg().contains("successfully queued");

      ScanFileResponse response = new ScanFileResponse(responseCode, isSuccessfullyQueued, scanFileResponse);

      callback.completed(response);
    } catch(Exception e) {
      callback.completed(new ScanFileResponse(ResponseCode.PARSE_ERROR, e.getLocalizedMessage()));
    }
  }


  protected ResponseCode getErrorCode(WebClientResponse response) {
    // TODO: check if response code is 403 (HTTP_FORBIDDEN) or 204 (API_LIMIT_EXCEEDED) and tell user, that api key is incorrect or API limit exceeded
    return ResponseCode.NETWORK_ERROR;
  }

}
