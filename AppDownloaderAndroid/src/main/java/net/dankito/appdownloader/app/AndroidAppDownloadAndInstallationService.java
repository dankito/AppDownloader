package net.dankito.appdownloader.app;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.support.v7.app.AlertDialog;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.model.AppDownloadInfo;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.AppState;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.AlertHelper;
import net.dankito.appdownloader.util.web.DownloadResult;
import net.dankito.appdownloader.util.web.IDownloadCompletedCallback;
import net.dankito.appdownloader.util.web.IDownloadManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ganymed on 20/11/16.
 */

public class AndroidAppDownloadAndInstallationService implements IAppDownloadAndInstallationService {

  private static final Logger log = LoggerFactory.getLogger(AndroidAppDownloadAndInstallationService.class);


  protected Activity activity;

  protected List<IAppDownloader> appDownloaders;

  protected IDownloadManager downloadManager;

  protected IAppVerifier appVerifier;

  protected IAppInstaller appInstaller;


  public AndroidAppDownloadAndInstallationService(Activity activity, List<IAppDownloader> appDownloaders, IDownloadManager downloadManager,
                                                  IAppVerifier appVerifier, IAppInstaller appInstaller) {
    this.activity = activity;
    this.appDownloaders = appDownloaders;
    this.downloadManager = downloadManager;
    this.appVerifier = appVerifier;
    this.appInstaller = appInstaller;
  }


  @Override
  public void installApp(AppInfo app) {
    if(app.hasDownloadUrls()) {
      downloadApp(app, getBestAppDownloadUrl(app));
    }
    else {
      getAppDownloadLinkAndDownloadApp(app);
    }
  }

  protected AppDownloadInfo getBestAppDownloadUrl(AppInfo app) {
    List<AppDownloadInfo> downloadInfoFromNotSoTrustworthySources = new ArrayList<>();

    for(AppDownloadInfo downloadInfo : app.getDownloadInfos()) {
      if(downloadInfo.hasDownloadLink()) { // if it's a trustworthy source, return immediately, otherwise add to downloadInfoFromNotSoTrustworthySources and ...
        if(downloadInfo.getAppDownloader().isTrustworthySource()) {
          return downloadInfo;
        }

        downloadInfoFromNotSoTrustworthySources.add(downloadInfo);
      }
    }

    if(downloadInfoFromNotSoTrustworthySources.size() > 0) { // ... return it only if no download link from an absolute trustworthy source has been found
      return downloadInfoFromNotSoTrustworthySources.get(0);
    }
    return null;
  }

  protected void getAppDownloadLinkAndDownloadApp(final AppInfo app) {
    app.setState(AppState.GETTING_DOWNLOAD_URL);

    final AtomicBoolean hasDownloadUrlBeenRetrieved = new AtomicBoolean(false);
    final AtomicBoolean hasDownloadBeenStarted = new AtomicBoolean(false);
    final List<IAppDownloader> downloadersNotYetCompleted = new CopyOnWriteArrayList<>(appDownloaders);

    for(IAppDownloader appDownloader : appDownloaders) {
      appDownloader.getAppDownloadLinkAsync(app, new GetAppDownloadUrlResponseCallback() {
        @Override
        public void completed(GetAppDownloadUrlResponse response) {
          synchronized(hasDownloadUrlBeenRetrieved) {
            getAppDownloadLinkCompleted(app, response, hasDownloadUrlBeenRetrieved, hasDownloadBeenStarted, downloadersNotYetCompleted);
          }
        }
      });
    }
  }

  protected void getAppDownloadLinkCompleted(AppInfo app, GetAppDownloadUrlResponse response, AtomicBoolean hasDownloadUrlBeenRetrieved, AtomicBoolean hasDownloadBeenStarted, List<IAppDownloader> downloadersNotYetCompleted) {
    IAppDownloader appDownloader = response.getAppDownloader();
    downloadersNotYetCompleted.remove(appDownloader);

    AppDownloadInfo downloadInfo = response.getDownloadInfo();
    if(response.getDownloadInfo() != null) {
      app.addDownloadInfo(downloadInfo);
    }

    if(response.isSuccessful()) {
      hasDownloadUrlBeenRetrieved.set(true);

      if(hasDownloadBeenStarted.get() == false &&
          (appDownloader.isTrustworthySource() || areOnlyNotFullyTrustworthySourcesLeft(downloadersNotYetCompleted))) {
        hasDownloadBeenStarted.set(true);
        downloadApp(app, downloadInfo);
      }
    }

    if(downloadersNotYetCompleted.size() == 0) {
      if(hasDownloadUrlBeenRetrieved.get() == false) {
        app.setToItsDefaultState();
        showErrorMessageThreadSafe(activity.getString(R.string.error_message_could_not_download_app, response.getError()), null);
      }
      else if(hasDownloadBeenStarted.get() == false) {
        downloadApp(app, getBestAppDownloadUrl(app));
      }
    }
  }

  protected boolean areOnlyNotFullyTrustworthySourcesLeft(List<IAppDownloader> downloadersNotYetCompleted) {
    for(IAppDownloader appDownloader : downloadersNotYetCompleted) {
      if(appDownloader.isTrustworthySource()) {
        return false;
      }
    }

    return true;
  }

  protected void downloadApp(AppInfo app, AppDownloadInfo downloadInfo) {
    log.info("Starting to download App " + app + " from " + downloadInfo + " ...");

    app.setState(AppState.DOWNLOADING);

    downloadAppViaAndroidDownloadManager(app, downloadInfo);
  }

  protected void downloadAppViaAndroidDownloadManager(AppInfo app, AppDownloadInfo downloadInfo) {
    downloadManager.downloadUrlAsync(app, downloadInfo, new IDownloadCompletedCallback() {
      @Override
      public void completed(DownloadResult result) {
        appDownloadCompleted(result);
      }
    });
  }

  protected void appDownloadCompleted(DownloadResult result) {
    final AppDownloadInfo downloadInfo = result.getDownloadInfo();
    final AppInfo app = downloadInfo.getAppInfo();

    if(result.isSuccessful()) {
      appVerifier.verifyDownloadedApk(downloadInfo, new AppPackageVerificationCallback() {
        @Override
        public void completed(AppPackageVerificationResult verificationResult) {
          if(verificationResult.wasVerificationSuccessful()) {
            doAppInstall(downloadInfo);
          }
          else {
            showApkVerificationWasNotSuccessfulErrorMessage(app, downloadInfo, verificationResult);
          }
        }
      });
    }
    else if(result.isUserCancelled() == false) {
      showErrorMessageThreadSafe(result.getError(), activity.getResources().getString(R.string.error_message_could_not_download_app, app.getTitle()));
    }
  }

  protected void doAppInstall(AppDownloadInfo downloadInfo) {
    appInstaller.installApp(downloadInfo);
  }

  // TODO: this is the same code as in AppInstaller
  protected void deleteDownloadedFile(AppDownloadInfo downloadInfo) {
    try {
      File file = new File(downloadInfo.getDownloadLocationPath());
      if(file.exists()) {
        log.info("Deleting installed Apk file " + file.getAbsolutePath());
        file.delete();
      }
    } catch(Exception e) {
      log.error("Could not deleted installed Apk file " + downloadInfo.getDownloadLocationPath(), e);
    }
  }

  protected void showApkVerificationWasNotSuccessfulErrorMessage(final AppInfo app, final AppDownloadInfo downloadInfo, AppPackageVerificationResult verificationResult) {
    Resources resources = activity.getResources();

    String errorMessageTitle = resources.getString(R.string.error_message_title_could_not_verify_apk_file, app.getTitle());
    String errorMessage = resources.getString(R.string.error_message_could_not_verify_apk_file, verificationResult.getErrorMessage());

    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder = builder.setMessage(errorMessage);
    builder = builder.setTitle(errorMessageTitle);

    builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        deleteDownloadedFile(downloadInfo);
      }
    });
    builder.setNeutralButton(R.string.install_anyway, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialogInterface, int i) {
        doAppInstall(downloadInfo);
      }
    });

    builder.create().show();
  }

  protected void showErrorMessageThreadSafe(String error, String title) {
    AlertHelper.showErrorMessageThreadSafe(activity, error, title);
  }

}
