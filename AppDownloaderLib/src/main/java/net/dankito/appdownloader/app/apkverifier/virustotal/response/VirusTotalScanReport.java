package net.dankito.appdownloader.app.apkverifier.virustotal.response;

/**
 * Created by ganymed on 22/11/16.
 */

public class VirusTotalScanReport {

  protected boolean detected;

  protected String version;

  protected String result;

  protected String update;


  public boolean isDetected() {
    return detected;
  }

  public void setDetected(boolean detected) {
    this.detected = detected;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public String getUpdate() {
    return update;
  }

  public void setUpdate(String update) {
    this.update = update;
  }
}
