package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 18/11/16.
 */

public enum HashAlgorithm {

  MD5("MD5"),
  SHA1("SHA");


  private String algorithmName;


  HashAlgorithm(String algorithmName) {
    this.algorithmName = algorithmName;
  }


  public String getAlgorithmName() {
    return algorithmName;
  }

}
