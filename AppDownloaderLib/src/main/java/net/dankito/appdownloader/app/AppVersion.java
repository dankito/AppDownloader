package net.dankito.appdownloader.app;

/**
 * Created by ganymed on 19/11/16.
 */

public class AppVersion implements Comparable<AppVersion> {

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

    int compareResult = compareTwoIntegers(getMajor(), other.getMajor());
    if(compareResult != 0) {
      return compareResult;
    }

    compareResult = compareTwoIntegers(getMinor(), other.getMinor());
    if(compareResult != 0) {
      return compareResult;
    }

    compareResult = compareTwoIntegers(getRevision(), other.getRevision());
    if(compareResult != 0) {
      return compareResult;
    }

    compareResult = compareTwoIntegers(getBuild(), other.getBuild());
    return compareResult;
  }

  protected int compareTwoIntegers(Integer integer1, Integer integer2) {
    if(integer1 == null && integer2 == null) {
      return 0;
    }
    else if(integer1 != null && integer2 == null) {
      return 1;
    }
    else if(integer1 == null && integer2 != null) {
      return -1;
    }

    return integer1.compareTo(integer2);
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
    } catch(Exception ignored) { }

    return null;
  }

  static Integer tryToParseToInteger(String versionPart) {
    try {
      return Integer.parseInt(versionPart);
    } catch(Exception ignored) { }

    return null;
  }

}
