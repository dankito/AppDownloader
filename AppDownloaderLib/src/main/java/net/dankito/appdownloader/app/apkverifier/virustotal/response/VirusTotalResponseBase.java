package net.dankito.appdownloader.app.apkverifier.virustotal.response;

/**
 * Created by ganymed on 04/12/16.
 */

public abstract class VirusTotalResponseBase {

  protected int response_code;

  protected String verbose_msg;

  protected String scan_id;

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

  public String getScan_id() {
    return scan_id;
  }

  public void setScan_id(String scan_id) {
    this.scan_id = scan_id;
  }

  public String getPermalink() {
    return permalink;
  }

  public void setPermalink(String permalink) {
    this.permalink = permalink;
  }

}
