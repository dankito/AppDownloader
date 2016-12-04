package net.dankito.appdownloader.app.apkverifier.virustotal.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by ganymed on 04/12/16.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class VirusTotalFileScanResponse extends VirusTotalResponseBase {

  protected String resource;

  protected String sha256;

  protected String md5;

  protected String sha1;


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

}
