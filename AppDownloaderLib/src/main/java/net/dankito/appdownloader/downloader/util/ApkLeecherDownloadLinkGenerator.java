package net.dankito.appdownloader.downloader.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * As for downloading from ApkLeecher JavaScript must be executed.
 * So we try to generate download link manually.
 */
public class ApkLeecherDownloadLinkGenerator {

  protected static String DOWNLOAD_URL_SUFFIX_1;

  protected static String DOWNLOAD_URL_SUFFIX_2;

  private static final Logger log = LoggerFactory.getLogger(ApkLeecherDownloadLinkGenerator.class);


  protected String appNameAndVersion;

  protected String lastUpdatedOn;


  public ApkLeecherDownloadLinkGenerator() {
    try {
      DOWNLOAD_URL_SUFFIX_1 = "_" + URLEncoder.encode("[www.apkleecher.com].apk", "ASCII");
      DOWNLOAD_URL_SUFFIX_2 = "_" + URLEncoder.encode("[www.Apkleecher.com].apk", "ASCII");
    } catch(Exception ignored) { }
  }


  /**
   * The actual download url is not absolute certain.
   * Sometimes it contains www.apkleecher.com, sometimes www.Apkleecher.com.
   * Sometimes there's a underscore at the end of the app name, mostly not.
   * @return
   */
  public List<String> generateDownloadUrlVariants() {
    List<String> downloadUrlVariants = new ArrayList<>();

    try {
      int indexAppNameAndVersionSeparator = appNameAndVersion.lastIndexOf('.'); // sometimes there's a string at the end of the version, e.g. ' lite'
      indexAppNameAndVersionSeparator = appNameAndVersion.lastIndexOf(' ', indexAppNameAndVersionSeparator);

      String version = appNameAndVersion.substring(indexAppNameAndVersionSeparator);
      String appName = appNameAndVersion.substring(0, indexAppNameAndVersionSeparator);

      String formattedAppName = getAppName(appName);

      String formattedUpdateString = lastUpdatedOn.replace('-', '/');

      String formattedVersion = version.replace(" ", "%20");

      String urlBase = "http://apkleecher.com/apps/" + formattedUpdateString + "/" + formattedAppName;

      downloadUrlVariants.add(urlBase + formattedVersion + DOWNLOAD_URL_SUFFIX_1);
      downloadUrlVariants.add(urlBase + formattedVersion + DOWNLOAD_URL_SUFFIX_2);

      if(appName.endsWith(")") || appName.endsWith("]")) {
        downloadUrlVariants.add(urlBase + "_" + formattedVersion + DOWNLOAD_URL_SUFFIX_1);
        downloadUrlVariants.add(urlBase + "_" + formattedVersion + DOWNLOAD_URL_SUFFIX_2);
      }
    } catch (Exception e) {
      log.error("Could not generate download link", e);
    }

    return downloadUrlVariants;
  }

  protected String getAppName(String appName) {
    String formattedAppName = appName.replaceAll("[^A-Za-z0-9 ]", " ").trim();

    formattedAppName = formattedAppName.replace("   ", " ").replace("  ", " ").replace("  ", " ");
    formattedAppName = formattedAppName.replace(' ', '_');

    return formattedAppName;
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
