package net.dankito.appdownloader.app.apkverifier.virustotal;

import net.dankito.appdownloader.app.apkverifier.DownloadedApkInfo;
import net.dankito.appdownloader.app.apkverifier.IApkFileVerifier;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileCallback;
import net.dankito.appdownloader.app.apkverifier.VerifyApkFileResult;
import net.dankito.appdownloader.app.apkverifier.virustotal.calback.GetFileScanReportCallback;
import net.dankito.appdownloader.app.apkverifier.virustotal.calback.ScanFileCallback;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.GetFileScanReportResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.ScanFileResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanReportResponse;
import net.dankito.appdownloader.app.apkverifier.virustotal.response.VirusTotalFileScanResponse;
import net.dankito.appdownloader.app.model.AppState;
import net.dankito.appdownloader.util.web.IWebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by ganymed on 22/11/16.
 */

public class VirusTotalApkFileVerifier implements IApkFileVerifier {

  public static final int MAX_NUMBER_OF_POSITIVE_FILE_SCANS_TO_JUDGE_SECURE = 2;

  protected static final int CHECK_IF_FILE_SCAN_REPORT_IS_AVAILABLE_PERIOD_MILLIS = 60 * 1000; // every minute


  private static final String VIRUS_TOTAL_PROPERTIES_FILENAME = "virus_total.properties";

  private static final String VIRUS_TOTAL_API_KEY_PROPERTY_NAME = "api.key";

  private static final String COULD_NOT_LOAD_API_KEY = null;


  private static final Logger log = LoggerFactory.getLogger(VirusTotalApkFileVerifier.class);


  protected VirusTotalApiV2Client virusTotalClient;

  protected IWebClient webClient;


  public VirusTotalApkFileVerifier(IWebClient webClient) {
    this.virusTotalClient = new VirusTotalApiV2Client(readApiKey(), webClient);
  }


  public void verifyApkFile(final DownloadedApkInfo downloadedApkInfo, final VerifyApkFileCallback callback) {
    final FileScanRequest request = new FileScanRequest(downloadedApkInfo.getDownloadSource().getDownloadLocationPath(), downloadedApkInfo.getSha256CheckSum(), "application/vnd.android.package-archive");

    virusTotalClient.getFileScanReportAsync(request, new GetFileScanReportCallback() {
      @Override
      public void completed(GetFileScanReportResponse response) {
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

  protected VerifyApkFileResult createVerifyApkFileResultFromFileScanReport(GetFileScanReportResponse response, DownloadedApkInfo downloadedApkInfo) {
    VirusTotalFileScanReportResponse fileScanReportResponse = response.getResponse();
    boolean knowsApkFile = fileScanReportResponse.getTotal() > 0; // response successful and at least one file scan
    boolean isSecure = fileScanReportResponse.getPositives() < MAX_NUMBER_OF_POSITIVE_FILE_SCANS_TO_JUDGE_SECURE; // sometimes there are false positives

    return new VerifyApkFileResult(downloadedApkInfo, knowsApkFile, isSecure, true);
  }

  protected void startFileScan(final DownloadedApkInfo downloadedApkInfo, final FileScanRequest request, final VerifyApkFileCallback callback) {
    virusTotalClient.scanFileAsync(request, new ScanFileCallback() {
      @Override
      public void completed(ScanFileResponse response) {
        if(response.isSuccessful() == false) {
          callback.completed(new VerifyApkFileResult(downloadedApkInfo, response.getError()));
        }
        else {
          periodicallyCheckIfScanReportAvailable(downloadedApkInfo, request, response, callback);
        }
      }
    });
  }

  protected void periodicallyCheckIfScanReportAvailable(final DownloadedApkInfo downloadedApkInfo, final FileScanRequest request, ScanFileResponse response, final VerifyApkFileCallback callback) {
    downloadedApkInfo.getApp().setState(AppState.SCANNING_FOR_VIRUSES);

    VirusTotalFileScanResponse scanResponse = response.getResponse();
    request.setSha256Checksum(scanResponse.getSha256());
    request.setScanId(scanResponse.getScan_id());

    final Timer checkIfScanReportAvailableTimer = new Timer();
    checkIfScanReportAvailableTimer.schedule(new TimerTask() {
      @Override
      public void run() {
        checkIfFileScanReportIsAvailable(downloadedApkInfo, request, checkIfScanReportAvailableTimer, callback);
      }
    }, CHECK_IF_FILE_SCAN_REPORT_IS_AVAILABLE_PERIOD_MILLIS, CHECK_IF_FILE_SCAN_REPORT_IS_AVAILABLE_PERIOD_MILLIS);
  }

  protected void checkIfFileScanReportIsAvailable(final DownloadedApkInfo downloadedApkInfo, FileScanRequest request, final Timer checkIfScanReportAvailableTimer, final VerifyApkFileCallback callback) {
    virusTotalClient.getFileScanReportAsync(request, new GetFileScanReportCallback() {
      @Override
      public void completed(GetFileScanReportResponse response) {
        if(response.isSuccessful() && response.getResponseCode() == ResponseCode.ITEM_FOUND) { // TODO: implement other response codes
          checkIfScanReportAvailableTimer.cancel();
          checkIfScanReportAvailableTimer.purge();

          callback.completed(createVerifyApkFileResultFromFileScanReport(response, downloadedApkInfo));
        }
      }
    });
  }



  protected String readApiKey() {
    try {
      InputStream propertiesInputStream = getClass().getClassLoader().getResourceAsStream(VIRUS_TOTAL_PROPERTIES_FILENAME);

      Properties virusTotalProperties = new Properties();
      virusTotalProperties.load(propertiesInputStream);

      propertiesInputStream.close();

      return virusTotalProperties.getProperty(VIRUS_TOTAL_API_KEY_PROPERTY_NAME);
    } catch(Exception e) {
      log.error("Could not load VirusTotal API key", e);
    }

    return COULD_NOT_LOAD_API_KEY;
  }

}
