package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ganymed on 22/11/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusTotalFileScanReportResponse {

  protected int response_code;

  protected String verbose_msg;

  protected String resource;

  protected String scan_id;

  protected String md5;

  protected String sha1;

  protected String sha256;

  protected String scan_date;

  protected int positives;

  protected int total;

  protected Map<String, VirusTotalScanReport> scans = new HashMap<>();

  protected String permalink;


  public int getResponse_code() {
    return response_code;
  }

  public void setResponse_code(int response_code) {
    this.response_code = response_code;
  }

  public String getVerbose_msg() {
    return verbose_msg;
  }

  public void setVerbose_msg(String verbose_msg) {
    this.verbose_msg = verbose_msg;
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getScan_id() {
    return scan_id;
  }

  public void setScan_id(String scan_id) {
    this.scan_id = scan_id;
  }

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

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
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

  public String getPermalink() {
    return permalink;
  }

  public void setPermalink(String permalink) {
    this.permalink = permalink;
  }

  public Map<String, VirusTotalScanReport> getScans() {
    return scans;
  }

  public void setScans(Map<String, VirusTotalScanReport> scans) {
    this.scans = scans;
  }

}
