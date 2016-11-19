package net.dankito.appdownloader.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ganymed on 19/11/16.
 */

public class AppVersion implements Comparable<AppVersion> {

  private static final Logger log = LoggerFactory.getLogger(AppVersion.class);

  protected Integer major = null;

  protected Integer minor = null;

  protected Integer revision = null;

  protected Integer build = null;


  public AppVersion() {

  }

  public AppVersion(int major) {
    this.major = major;
  }

  public AppVersion(int major, int minor) {
    this(major);
    this.minor = minor;
  }

  public AppVersion(int major, int minor, int revision) {
    this(major, minor);
    this.revision = revision;
  }

  public AppVersion(int major, int minor, int revision, int build) {
    this(major, minor, revision);
    this.build = build;
  }


  public Integer getMajor() {
    return major;
  }

  public void setMajor(Integer major) {
    this.major = major;
  }

  public Integer getMinor() {
    return minor;
  }

  public void setMinor(Integer minor) {
    this.minor = minor;
  }

  public Integer getRevision() {
    return revision;
  }

  public void setRevision(Integer revision) {
    this.revision = revision;
  }

  public Integer getBuild() {
    return build;
  }

  public void setBuild(Integer build) {
    this.build = build;
  }


  public String getVersionString() {
    String versionString = "0";

    if(major != null) {
      versionString = "" + major;

      if(minor != null) {
        versionString += "." + minor;

        if(revision != null) {
          versionString += "." + revision;

          if(build != null) {
            versionString += "." + build;
          }
        }
      }
    }

    return versionString;
  }


  @Override
  public String toString() {
    return getVersionString();
  }


  @Override
  public int compareTo(AppVersion other) {
    if(other == null) {
      return 1;
    }

    if(getMajor() > other.getMajor()) {
      return 1;
    }
    else if(getMajor() < other.getMajor()) {
      return -1;
    }

    if(getMinor() > other.getMinor()) {
      return 1;
    }
    else if(getMinor() < other.getMinor()) {
      return -1;
    }

    if(getRevision() > other.getRevision()) {
      return 1;
    }
    else if(getRevision() < other.getRevision()) {
      return -1;
    }

    if(getBuild() > other.getBuild()) {
      return 1;
    }
    else if(getBuild() < other.getBuild()) {
      return -1;
    }

    return 0;
  }


  public static AppVersion parse(String versionString) {
    try {
      AppVersion appVersion = new AppVersion();
      versionString = versionString.replace('-', '.'); // sometimes revision is separated by '-' from minor
      versionString = versionString.replace(" (", ".").replace(")", ""); // sometimes revision or build are in braces with a leading white space
      versionString = versionString.replaceAll("[^\\d.]", ""); // remove non numeric symbols
      String[] versionParts = versionString.split("\\.");

      if(versionParts.length > 0) {
        appVersion.setMajor(tryToParseToInteger(versionParts[0]));
      }

      if(versionParts.length > 1) {
        appVersion.setMinor(tryToParseToInteger(versionParts[1]));
      }

      if(versionParts.length > 2) {
        appVersion.setRevision(tryToParseToInteger(versionParts[2]));
      }

      if(versionParts.length > 3) {
        appVersion.setBuild(tryToParseToInteger(versionParts[3]));
      }

      return appVersion;
    } catch(Exception e) {
      log.error("Could not parse version string " + versionString + " to AppVersion", e);
    }

    return null;
  }

  static Integer tryToParseToInteger(String versionPart) {
    try {
      return Integer.parseInt(versionPart);
    } catch(Exception ignored) {
      log.error("Could not parse " + versionPart + " to Integer", ignored); // TODO: remove log output
    }

    return null;
  }

}