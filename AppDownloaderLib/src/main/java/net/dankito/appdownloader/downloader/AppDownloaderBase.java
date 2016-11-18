package net.dankito.appdownloader.downloader;

import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.responses.DownloadAppResponse;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.DownloadAppCallback;
import net.dankito.appdownloader.responses.callbacks.DownloadProgressListener;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.IThreadPool;
import net.dankito.appdownloader.util.web.IWebClient;
import net.dankito.appdownloader.util.web.RequestCallback;
import net.dankito.appdownloader.util.web.RequestParameters;
import net.dankito.appdownloader.util.web.WebClientResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * Created by ganymed on 04/11/16.
 */

public abstract class AppDownloaderBase implements IAppDownloader {

  public static final int DOWNLOAD_CONNECTION_TIMEOUT_MILLIS = 2000;

  public static final int COUNT_CONNECTION_RETRIES = 2;


  private static final Logger log = LoggerFactory.getLogger(AppDownloaderBase.class);


  protected IWebClient webClient;

  protected IThreadPool threadPool;


  public AppDownloaderBase(IWebClient webClient, IThreadPool threadPool) {
    this.webClient = webClient;
    this.threadPool = threadPool;
  }


  public void downloadAppAsync(final AppInfo appToDownload, final DownloadAppCallback callback) {
    threadPool.runAsync(new Runnable() {
      @Override
      public void run() {
        getAppDownloadLinkAsync(appToDownload, new GetAppDownloadUrlResponseCallback() {
          @Override
          public void completed(GetAppDownloadUrlResponse response) {
            if(response.isSuccessful() == false) {
              callback.completed(new DownloadAppResponse(response.getError()));
            }
            else {
              saveAppToFile(appToDownload, response, callback);
            }
          }
        });
      }
    });
  }

  protected void saveAppToFile(final AppInfo appToDownload, GetAppDownloadUrlResponse response, final DownloadAppCallback callback) {
    log.info("Starting to download App ...");

    try {
      final File targetFile = File.createTempFile(appToDownload.getTitle(), ".apk");
      final OutputStream outStream = new FileOutputStream(targetFile);

      RequestParameters parameters = createRequestParametersWithDefaultValues(response.getUrl());
      parameters.setHasStringResponse(false);

      parameters.setDownloadProgressListener(new DownloadProgressListener() {
        @Override
        public void progress(float progress, byte[] downloadedChunk) {
          try {
            outStream.write(downloadedChunk);
          } catch(Exception e) {
            log.error("Could not write downloaded chunk to target file " + targetFile.getAbsolutePath(), e);
            // TODO: abort download and show message to user
          }
        }
      });

      webClient.getAsync(parameters, new RequestCallback() {
        @Override
        public void completed(WebClientResponse response) {
          try {
            outStream.flush();
            outStream.close();
          } catch(Exception ignored) { }

          if(response.isSuccessful() == false) {
            callback.completed(new DownloadAppResponse(response.getError()));
          }
          else {
            callback.completed(new DownloadAppResponse(appToDownload, targetFile));
          }
        }
      });
    } catch(Exception e) {
      log.error("Could not download App from " + response.getUrl(), e);
      callback.completed(new DownloadAppResponse(e.getLocalizedMessage()));
    }
  }


  protected RequestParameters createRequestParametersWithDefaultValues(String url) {
    RequestParameters parameters = new RequestParameters(url);
    parameters.setConnectionTimeoutMillis(DOWNLOAD_CONNECTION_TIMEOUT_MILLIS);
    parameters.setCountConnectionRetries(COUNT_CONNECTION_RETRIES);
    return parameters;
  }

}
