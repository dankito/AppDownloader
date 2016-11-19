package net.dankito.appdownloader.app;

import net.dankito.appdownloader.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class AppInfo {

  protected String packageName;

  protected String title;

  protected String developer;

  protected String smallCoverImageUrl;

  protected String largeCoverImageUrl;

  protected Object iconImage;

  protected String appDetailsPageUrl;

  protected boolean isAlreadyInstalled = false;

  protected String installedVersion = null;

  protected AppState state = AppState.INSTALLABLE;

  protected List<AppStateListener> stateListeners = new ArrayList<>();


  // from App Details Page

  protected String version;

  protected String rating;

  protected String countRatings;

  protected String countInstallations;

  // download process

  protected List<AppDownloadInfo> downloadInfos = new ArrayList<>();


  public AppInfo(String packageName) {
    this.packageName = packageName;
  }


  public String getPackageName() {
    return packageName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDeveloper() {
    return developer;
  }

  public void setDeveloper(String developer) {
    this.developer = developer;
  }


  public boolean hasSmallCoverImageUrl() {
    return StringUtils.isNotNullOrEmpty(getSmallCoverImageUrl());
  }

  public String getSmallCoverImageUrl() {
    return smallCoverImageUrl;
  }

  public void setSmallCoverImageUrl(String smallCoverImageUrl) {
    this.smallCoverImageUrl = smallCoverImageUrl;
  }

  public String getLargeCoverImageUrl() {
    return largeCoverImageUrl;
  }

  public void setLargeCoverImageUrl(String largeCoverImageUrl) {
    this.largeCoverImageUrl = largeCoverImageUrl;
  }

  public Object getIconImage() {
    return iconImage;
  }

  public void setIconImage(Object iconImage) {
    this.iconImage = iconImage;
  }

  public String getAppDetailsPageUrl() {
    return appDetailsPageUrl;
  }

  public void setAppDetailsPageUrl(String appDetailsPageUrl) {
    this.appDetailsPageUrl = appDetailsPageUrl;
  }

  public boolean isAlreadyInstalled() {
    return isAlreadyInstalled;
  }

  public void setAlreadyInstalled(boolean alreadyInstalled) {
    isAlreadyInstalled = alreadyInstalled;

    if(getState() == AppState.INSTALLABLE) {
      setToItsDefaultState();
    }
  }

  public String getInstalledVersion() {
    return installedVersion;
  }

  public void setInstalledVersion(String installedVersion) {
    this.installedVersion = installedVersion;
  }

  public AppState getState() {
    return state;
  }

  public void setState(AppState state) {
    AppState previousState = this.state;

    this.state = state;

    callStateListeners(state, previousState);
  }

  public boolean addStateListener(AppStateListener listener) {
    return stateListeners.add(listener);
  }

  public boolean removeStateListener(AppStateListener listener) {
    return stateListeners.remove(listener);
  }

  protected void callStateListeners(AppState newState, AppState previousState) {
    for(AppStateListener listener : stateListeners) {
      listener.stateChanged(newState, previousState);
    }
  }


  public boolean areNecessaryInformationSet() {
    return StringUtils.isNotNullOrEmpty(packageName) && StringUtils.isNotNullOrEmpty(title) && StringUtils.isNotNullOrEmpty(appDetailsPageUrl);
  }


  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getRating() {
    return rating;
  }

  public void setRating(String rating) {
    this.rating = rating;
  }

  public String getCountRatings() {
    return countRatings;
  }

  public void setCountRatings(String countRatings) {
    this.countRatings = countRatings;
  }

  public String getCountInstallations() {
    return countInstallations;
  }

  public void setCountInstallations(String countInstallations) {
    this.countInstallations = countInstallations;
  }


  public boolean areAppDetailsDownloaded() {
    return StringUtils.isNotNullOrEmpty(getVersion()) && StringUtils.isNotNullOrEmpty(getRating()) &&
        StringUtils.isNotNullOrEmpty(getCountRatings()) && StringUtils.isNotNullOrEmpty(getCountInstallations());
  }


  public boolean hasDownloadUrls() {
    return downloadInfos.size() > 0; // TODO: also check if they really contain a download url
  }

  public boolean addDownloadUrl(AppDownloadInfo appDownloadInfo) {
    return downloadInfos.add(appDownloadInfo);
  }

  public List<AppDownloadInfo> getDownloadInfos() {
    return downloadInfos;
  }

  public boolean isAlreadyDownloaded() {
    return StringUtils.isNotNullOrEmpty(getDownloadLocationPath());
  }

  public String getDownloadLocationPath() {
    for(AppDownloadInfo downloadInfo : getDownloadInfos()) {
      if(downloadInfo.isDownloaded()) {
        return downloadInfo.getDownloadLocationPath();
      }
    }

    return null;
  }


  public String getApkSignature() {
    for(AppDownloadInfo downloadInfo : getDownloadInfos()) {
      if(downloadInfo.isApkSignatureSet()) {
        return downloadInfo.getApkSignature();
      }
    }

    return null;
  }


  public void setToItsDefaultState() {
    if(isAlreadyInstalled() == false) {
      setState(AppState.INSTALLABLE);
    }
    else {
      if(isUpdatable()) {
        setState(AppState.UPDATABLE);
      }
      else {
        setState(AppState.REINSTALLABLE);
      }
    }
  }

  protected boolean isUpdatable() {
    return isNewerVersion(getVersion(), getInstalledVersion());
  }

  protected boolean isNewerVersion(String version1, String version2) {
    return true;
  }


  @Override
  public String toString() {
    return getTitle() + " (" + getDeveloper() + "; " + getPackageName() + ")";
  }

}
