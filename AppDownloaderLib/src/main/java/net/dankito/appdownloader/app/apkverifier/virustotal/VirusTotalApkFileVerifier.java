package net.dankito.appdownloader.app.apkverifier.virustotal;

import net.dankito.appdownloader.app.apkverifier.DownloadedApkInfo;
import net.dankito.appdownloader.app.apkverifier.IApkFileVerifier;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileCallback;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileResult;
import net.dankito.appdownloader.app.apkverifier.virustotal.calback.GetFileScanCallback;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.GetFileScanResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanReportResponse;
import net.dankito.appdownloader.util.web.IWebClient;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ganymed on 22/11/16.
 */

public class VirusTotalApkFileVerifier implements IApkFileVerifier {

  public static final int MAX_NUMBER_OF_POSITIVE_FILE_SCANS_TO_JUDGE_SECURE = 2;


  private static final String VIRUS_TOTAL_PROPERTIES_FILENAME = "virus_total.properties";

  private static final String VIRUS_TOTAL_API_KEY_PROPERTY_NAME = "api.key";



  protected VirusTotalApiV2Client virusTotalClient;

  protected IWebClient webClient;


  public VirusTotalApkFileVerifier(IWebClient webClient) throws Exception {
    this.virusTotalClient = new VirusTotalApiV2Client(readApiKey(), webClient);
  }


  public void verifyApkFile(final DownloadedApkInfo downloadedApkInfo, final VerifyApkFileCallback callback) {
    final FileScanRequest request = new FileScanRequest(downloadedApkInfo.getDownloadSource().getDownloadLocationPath(), downloadedApkInfo.getSha256CheckSum(), "application/vnd.android.package-archive");

    virusTotalClient.getFileScanReportAsync(request, new GetFileScanCallback() {
      @Override
      public void completed(GetFileScanResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new VerifyApkFileResult(downloadedApkInfo, response.getError())); // TODO: check responseCode, e.g. if API limit exceeded etc.
        }
        else {
          if(response.getResponseCode() == ResponseCode.ITEM_FOUND) {
            callback.completed(createVerifyApkFileResultFromFileScanReport(response, downloadedApkInfo));
          }
          else if (response.getResponseCode() == ResponseCode.ITEM_NOT_FOUND) {
            startFileScan(downloadedApkInfo, request, callback);
          }
        }
      }
    });
  }

  protected VerifyApkFileResult createVerifyApkFileResultFromFileScanReport(GetFileScanResponse response, DownloadedApkInfo downloadedApkInfo) {
    VirusTotalFileScanReportResponse fileScanReportResponse = response.getResponse();
    boolean knowsApkFile = fileScanReportResponse.getTotal() > 0; // response successful and at least one file scan
    boolean isSecure = fileScanReportResponse.getPositives() < MAX_NUMBER_OF_POSITIVE_FILE_SCANS_TO_JUDGE_SECURE; // sometimes there are false positives

    return new VerifyApkFileResult(downloadedApkInfo, knowsApkFile, isSecure, true);
  }

  protected void startFileScan(DownloadedApkInfo downloadedApkInfo, FileScanRequest request, VerifyApkFileCallback callback) {

  }



  protected String readApiKey() throws Exception {
    InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream(VIRUS_TOTAL_PROPERTIES_FILENAME);

    Properties virusTotalProperties = new Properties();
    virusTotalProperties.load(propertiesInputStream);

    propertiesInputStream.close();

    return virusTotalProperties.getProperty(VIRUS_TOTAL_API_KEY_PROPERTY_NAME);
  }

}
