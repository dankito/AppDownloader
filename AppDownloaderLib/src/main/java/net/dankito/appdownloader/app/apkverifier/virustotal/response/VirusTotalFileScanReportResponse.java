package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 22/11/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusTotalFileScanReportResponse extends VirusTotalFileScanResponse {

  protected String scan_date;

  protected int positives;

  protected int total;

  protected Map<String, VirusTotalScanReport> scans = new HashMap<>();



  public String getScan_date() {
    return scan_date;
  }

  public void setScan_date(String scan_date) {
    this.scan_date = scan_date;
  }

  public int getPositives() {
    return positives;
  }

  public void setPositives(int positives) {
    this.positives = positives;
  }

  public int getTotal() {
    return total;
  }

  public void setTotal(int total) {
    this.total = total;
  }

  public Map<String, VirusTotalScanReport> getScans() {
    return scans;
  }

  public void setScans(Map<String, VirusTotalScanReport> scans) {
    this.scans = scans;
  }

}
