package net.dankito.appdownloader.app.apkverifier.virustotal.response;

/**
 * Created by ganymed on 04/12/16.
 */

public abstract class VirusTotalFileScanResponseBase extends VirusTotalResponseBase {

  protected String resource;

  protected String sha256;


  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getSha256() {
    return sha256;
  }

  public void setSha256(String sha256) {
    this.sha256 = sha256;
  }

}
