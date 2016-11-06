package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class AppSearchResult {

  protected String packageName;

  protected String appUrl;

  protected String title;

  protected String developer;

  protected String smallCoverImageUrl;

  protected String largeCoverImageUrl;


  // from App Details Page

  protected String appDetailsPageHtml;

  protected String version;

  protected String rating;

  protected String countRatings;

  protected String countInstallations;

  // download process

  protected List<String> downloadUrls = new ArrayList<>();

  protected String downloadLocationUri = null;


  public AppSearchResult(String packageName) {
    this.packageName = packageName;
  }


  public String getPackageName() {
    return packageName;
  }

  public String getAppUrl() {
    return appUrl;
  }

  public void setAppUrl(String appUrl) {
    this.appUrl = appUrl;
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


  public boolean areNecessaryInformationSet() {
    return StringUtils.isNotNullOrEmpty(packageName) && StringUtils.isNotNullOrEmpty(appUrl) && StringUtils.isNotNullOrEmpty(title);
  }


  public String getAppDetailsPageHtml() {
    return appDetailsPageHtml;
  }

  public void setAppDetailsPageHtml(String appDetailsPageHtml) {
    this.appDetailsPageHtml = appDetailsPageHtml;
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
    return StringUtils.isNotNullOrEmpty(getAppDetailsPageHtml()) && StringUtils.isNotNullOrEmpty(getRating()) &&
        StringUtils.isNotNullOrEmpty(getVersion()) && StringUtils.isNotNullOrEmpty(getCountRatings()) && StringUtils.isNotNullOrEmpty(getCountInstallations());
  }


  public boolean hasDownloadUrls() {
    return downloadUrls.size() > 0;
  }

  public boolean addDownloadUrl(String appDownloadUrl) {
    return downloadUrls.add(appDownloadUrl);
  }

  public List<String> getDownloadUrls() {
    return downloadUrls;
  }

  public boolean isAlreadyDownloaded() {
    return downloadLocationUri != null;
  }

  public String getDownloadLocationUri() {
    return downloadLocationUri;
  }

  public void setDownloadLocationUri(String downloadLocationUri) {
    this.downloadLocationUri = downloadLocationUri;
  }

  @Override
  public String toString() {
    return getTitle() + " (" + getDeveloper() + "; " + getPackageName() + ")";
  }

}
