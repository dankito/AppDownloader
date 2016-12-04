package net.dankito.appdownloader.app.apkverifier.virustotal;

import com.fasterxml.jackson.databind.ObjectMapper;

import net.dankito.appdownloader.app.apkverifier.virustotal.calback.GetFileScanCallback;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.GetFileScanResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanReportResponse;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestCallback;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;
import net.dankito.appdownloader.util.web.model.StringRequestBodyPart;

/**
 * Created by ganymed on 04/12/16.
 */

public class VirusTotalApiV2Client {

  public static final String BASE_URL = "https://www.virustotal.com/vtapi/v2/";

  public static final String FILE_SCAN_REPORT_SUB_URL = "file/report";


  private static final String API_KEY_FIELD = "apikey";

  private static final String RESOURCE_FIELD = "resource";

  private static final String FILE_FIELD = "file";



  protected String apiKey;

  protected IWebClient webClient;

  protected ObjectMapper mapper = new ObjectMapper();


  public VirusTotalApiV2Client(String apiKey, IWebClient webClient) {
    this.apiKey = apiKey;
    this.webClient = webClient;
  }


  public void getFileScanReportAsync(final FileScanRequest request, final GetFileScanCallback callback) {
    RequestParameters parameters = new RequestParameters(BASE_URL + FILE_SCAN_REPORT_SUB_URL);

    parameters.addRequestBodyPart(new StringRequestBodyPart(API_KEY_FIELD, apiKey));
    parameters.addRequestBodyPart(new StringRequestBodyPart(RESOURCE_FIELD, request.getSha256Checksum()));

    webClient.postAsync(parameters, new RequestCallback() {
      @Override
      public void completed(WebClientResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(createRequestFailedResult(response));
        }
        else {
          parseGetFileScanReportResponse(response, callback);
        }
      }
    });
  }

  protected void parseGetFileScanReportResponse(WebClientResponse webClientResponse, GetFileScanCallback callback) {
    try {
      String responseBody = webClientResponse.getBody();
      VirusTotalFileScanReportResponse fileScanReportResponse = mapper.readValue(responseBody, VirusTotalFileScanReportResponse.class);

      ResponseCode responseCode = ResponseCode.fromCode(fileScanReportResponse.getResponse_code());
      GetFileScanResponse response = new GetFileScanResponse(responseCode, fileScanReportResponse);

      callback.completed(response);
    } catch(Exception e) {
      callback.completed(new GetFileScanResponse(ResponseCode.PARSE_ERROR, e.getLocalizedMessage()));
    }
  }


  protected GetFileScanResponse createRequestFailedResult(WebClientResponse response) {
    // TODO: check if response code is 403 (HTTP_FORBIDDEN) or 204 (API_LIMIT_EXCEEDED) and tell user, that api key is incorrect or API limit exceeded
    return new GetFileScanResponse(ResponseCode.NETWORK_ERROR, response.getError());
  }

}
