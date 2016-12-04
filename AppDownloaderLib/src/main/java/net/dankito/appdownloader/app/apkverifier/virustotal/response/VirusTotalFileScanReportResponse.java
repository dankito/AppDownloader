package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 22/11/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusTotalFileScanReportResponse extends VirusTotalFileScanResponseBase {

  protected String md5;

  protected String sha1;

  protected String scan_date;

  protected int positives;

  protected int total;

  protected Map<String, VirusTotalScanReport> scans = new HashMap<>();

  public String getMd5() {
    return md5;
  }

  public void setMd5(String md5) {
    this.md5 = md5;
  }

  public String getSha1() {
    return sha1;
  }

  public void setSha1(String sha1) {
    this.sha1 = sha1;
  }

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
