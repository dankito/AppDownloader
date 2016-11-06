package net.dankito.appdownloader.util.web;

import java.util.Date;

/**
 * Created by ganymed on 06/11/16.
 */

public class EnqueuedDownload {

  protected long id;

  protected String uri;

  protected String downloadLocationUri;

  protected long fileSize;

  protected long bytesDownloadedSoFar;

  protected String status;

  protected String reason;

  protected String title;

  protected String description;

  protected Date lastModified;

  protected String mediaType;

  protected String mediaProviderUri;


  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getDownloadLocationUri() {
    return downloadLocationUri;
  }

  public void setDownloadLocationUri(String downloadLocationUri) {
    this.downloadLocationUri = downloadLocationUri;
  }

  public long getFileSize() {
    return fileSize;
  }

  public void setFileSize(long fileSize) {
    this.fileSize = fileSize;
  }

  public long getBytesDownloadedSoFar() {
    return bytesDownloadedSoFar;
  }

  public void setBytesDownloadedSoFar(long bytesDownloadedSoFar) {
    this.bytesDownloadedSoFar = bytesDownloadedSoFar;
  }

  public boolean wasDownloadSuccessful() {
    return getFileSize() == getBytesDownloadedSoFar();
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Date getLastModified() {
    return lastModified;
  }

  public void setLastModified(Date lastModified) {
    this.lastModified = lastModified;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getMediaProviderUri() {
    return mediaProviderUri;
  }

  public void setMediaProviderUri(String mediaProviderUri) {
    this.mediaProviderUri = mediaProviderUri;
  }
}
