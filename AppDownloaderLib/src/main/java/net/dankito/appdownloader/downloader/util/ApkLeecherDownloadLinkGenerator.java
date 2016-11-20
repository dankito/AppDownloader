package net.dankito.appdownloader.downloader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * As for downloading from ApkLeecher JavaScript must be executed.
 * So we try to generate download link manually.
 */
public class ApkLeecherDownloadLinkGenerator {

  private static final Logger log = LoggerFactory.getLogger(ApkLeecherDownloadLinkGenerator.class);


  protected String appNameAndVersion;

  protected String lastUpdatedOn;


  public ApkLeecherDownloadLinkGenerator() {

  }


  public String generateDownloadUrl() {
    try {
      int indexAppNameAndVersionSeparator = appNameAndVersion.lastIndexOf(' ');
      String version = appNameAndVersion.substring(indexAppNameAndVersionSeparator + 1);

      String appName = getAppName(appNameAndVersion, indexAppNameAndVersionSeparator);

      String formattedUpdateString = lastUpdatedOn.replace('-', '/');

      return "http://apkleecher.com/apps/" + formattedUpdateString + "/" + appName + "%20" + version + "_[www.apkleecher.com].apk";
    } catch(Exception e) {
      log.error("Could not generate download link", e);
    }

    return null;
  }

  protected String getAppName(String appNameAndVersion, int indexAppNameAndVersionSeparator) {
    String appName = appNameAndVersion.substring(0, indexAppNameAndVersionSeparator);

    appName = appName.replace("-", " ").replace("&", " ").replace(":", " ").replace("(", " ").replace(")", " ");
    appName = appName.replace("  ", " ");
    appName = appName.replace(' ', '_');

    return appName;
  }


  public String getAppNameAndVersion() {
    return appNameAndVersion;
  }

  public void setAppNameAndVersion(String appNameAndVersion) {
    this.appNameAndVersion = appNameAndVersion;
  }

  public String getLastUpdatedOn() {
    return lastUpdatedOn;
  }

  public void setLastUpdatedOn(String lastUpdatedOn) {
    this.lastUpdatedOn = lastUpdatedOn;
  }

}
