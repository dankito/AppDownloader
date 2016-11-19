package net.dankito.appdownloader.dialogs;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.AppDownloadLink;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.AppState;
import net.dankito.appdownloader.app.AppStateListener;
import net.dankito.appdownloader.di.AndroidDiComponent;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.AlertHelper;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.util.app.IAppInstaller;
import net.dankito.appdownloader.util.app.IAppVerifier;
import net.dankito.appdownloader.util.web.DownloadResult;
import net.dankito.appdownloader.util.web.IDownloadCompletedCallback;
import net.dankito.appdownloader.util.web.IDownloadManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

/**
 * Created by ganymed on 06/11/16.
 */

public class AppDetailsDialog extends FullscreenDialog {

  private static final Logger log = LoggerFactory.getLogger(AppDetailsDialog.class);


  protected AppInfo appInfo;

  protected List<IAppDownloader> appDownloaders;

  @Inject
  protected IDownloadManager downloadManager;

  @Inject
  protected IAppVerifier appVerifier;

  @Inject
  protected IAppInstaller appInstaller;

  @Inject
  protected IOnUiThreadRunner onUiThreadRunner;

  protected WebView wbvwViewAppDetails;

  protected View installAppActionView;

  protected ProgressBar prgbrInstallationProgress;

  protected TextView txtvwInstallationStep;


  public AppDetailsDialog(AppCompatActivity context) {
    super(context);
  }


  public void setAppInfo(AppInfo appInfo) {
    this.appInfo = appInfo;

    appInfo.addStateListener(appStateListener);
  }

  public void setAppDownloaders(List<IAppDownloader> appDownloaders) {
    this.appDownloaders = appDownloaders;
  }


  @Override
  protected void injectComponents(AndroidDiComponent component) {
    super.injectComponents(component);

    component.inject(this);
  }

  @Override
  protected int getLayoutId() {
    return R.layout.fragment_app_details;
  }

  @Override
  protected void setupUi(View rootView) {
    setupWebView(rootView);
  }

  protected void setupWebView(View rootView) {
    wbvwViewAppDetails = (WebView)rootView.findViewById(R.id.wbvwViewAppDetails);

    wbvwViewAppDetails.setHorizontalScrollBarEnabled(true);
    wbvwViewAppDetails.setVerticalScrollBarEnabled(true);

    WebSettings settings = wbvwViewAppDetails.getSettings();
    settings.setDefaultTextEncodingName("utf-8"); // otherwise non ASCII text doesn't get displayed correctly
    settings.setDefaultFontSize(11); // default font is way to large
    settings.setJavaScriptEnabled(true); // so that embedded videos etc. work
  }


  @Override
  public void show() {
    if(wbvwViewAppDetails != null) {
      clearWebViewViewAppDetails();
    }

    super.show();

    setAppValues();
  }

  protected void setAppValues() {
    toolbar.setTitle(appInfo.getTitle());

    wbvwViewAppDetails.loadUrl(appInfo.getAppDetailsPageUrl());

    invalidateOptionsMenu();

    setActionMenuInstallAppState();
  }


  @Override
  public void hide() {
    dialogIsAboutToHide();

    super.hide();
  }

  @Override
  public void onBackPressed() {
    dialogIsAboutToHide();

    super.onBackPressed();
  }

  protected void dialogIsAboutToHide() {
    clearWebViewViewAppDetails();
  }

  protected void clearWebViewViewAppDetails() {
    wbvwViewAppDetails.loadUrl("about:blank");
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    MenuInflater menuInflater = activity.getMenuInflater();

    menuInflater.inflate(R.menu.menu_app_details_dialog, menu);

    MenuItem installAppItem = menu.findItem(R.id.mnitmInstallApp);
    installAppActionView = installAppItem.getActionView();

    prgbrInstallationProgress = (ProgressBar)installAppActionView.findViewById(R.id.prgbrInstallationProgress);
    prgbrInstallationProgress.setIndeterminate(true);

    txtvwInstallationStep = (TextView)installAppActionView.findViewById(R.id.txtvwInstallationStep);

    installAppActionView.setOnClickListener(mnitmInstallAppClickListener);

    return super.onCreateOptionsMenu(menu);
  }

  protected View.OnClickListener mnitmInstallAppClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      mnitmInstallAppClicked();
    }
  };

  protected void mnitmInstallAppClicked() {
    installApp();
  }

  protected void installApp() {
    if(appInfo.isAlreadyDownloaded() == false) {
      if(appInfo.hasDownloadUrls()) {
        downloadApp(appInfo, getBestAppDownloadUrl(appInfo));
      }
      else {
        getAppDownloadLinkAndDownloadApp(appInfo);
      }
    }
  }

  protected AppDownloadLink getBestAppDownloadUrl(AppInfo appInfo) {
    if(appInfo.getDownloadLinks().size() == 1) {
      return appInfo.getDownloadLinks().get(0);
    }
    else {
      // TODO: choose best one
      return appInfo.getDownloadLinks().get(0);
    }
  }

  protected void getAppDownloadLinkAndDownloadApp(final AppInfo clickedApp) {
    appInfo.setState(AppState.GETTING_DOWNLOAD_URL);

    final AtomicBoolean hasDownloadUrlBeenRetrieved = new AtomicBoolean(false);
    final AtomicInteger countRequestsAppDownloadLinkCompleted = new AtomicInteger(0);

    for(IAppDownloader appDownloader : appDownloaders) {
      appDownloader.getAppDownloadLinkAsync(clickedApp, new GetAppDownloadUrlResponseCallback() {
        @Override
        public void completed(GetAppDownloadUrlResponse response) {
          synchronized(hasDownloadUrlBeenRetrieved) {
            getAppDownloadLinkCompleted(clickedApp, response, hasDownloadUrlBeenRetrieved, countRequestsAppDownloadLinkCompleted);
          }
        }
      });
    }
  }

  protected void getAppDownloadLinkCompleted(AppInfo clickedApp, GetAppDownloadUrlResponse response, AtomicBoolean hasDownloadUrlBeenRetrieved, AtomicInteger countRequestsAppDownloadLinkCompleted) {
    countRequestsAppDownloadLinkCompleted.incrementAndGet();

    if(response.isSuccessful()) {
      clickedApp.addDownloadUrl(response.getDownloadLink());
    }

    if(hasDownloadUrlBeenRetrieved.get() == false) {
      if(response.isSuccessful() == false) {
        if(countRequestsAppDownloadLinkCompleted.get() == appDownloaders.size()) { // only show error message if it's been the last AppDownloader which's request completed
          AlertHelper.showErrorMessageThreadSafe(activity, activity.getString(R.string.error_message_could_not_download_app, response.getError()));
        }
      }
      else {
        downloadApp(clickedApp, response.getDownloadLink());
      }
    }

    if(response.isSuccessful()) {
      hasDownloadUrlBeenRetrieved.set(true);
    }
  }

  protected void downloadApp(AppInfo clickedApp, AppDownloadLink downloadLink) {
    log.info("Starting to download App " + clickedApp + " from " + downloadLink + " ...");

    appInfo.setState(AppState.DOWNLOADING);

    downloadAppViaAndroidDownloadManager(clickedApp, downloadLink);
  }

  protected void downloadAppViaAndroidDownloadManager(AppInfo clickedApp, AppDownloadLink downloadLink) {
    downloadManager.downloadUrlAsync(clickedApp, downloadLink, new IDownloadCompletedCallback() {
      @Override
      public void completed(DownloadResult result) {
        appDownloadCompleted(result);
      }
    });
  }

  protected void appDownloadCompleted(DownloadResult result) {
    AppDownloadLink downloadLink = result.getDownloadLink();
    AppInfo appInfo = downloadLink.getAppInfo();

    if(result.isSuccessful()) {
      if(appVerifier.verifyDownloadedApk(downloadLink)) {
        appInstaller.installApp(downloadLink);
      }
      else {
        Resources resources = activity.getResources();
        String errorMessageTitle = resources.getString(R.string.error_message_title_could_not_verify_app_package, appInfo.getTitle());
        showErrorMessageThreadSafe(errorMessageTitle, null);
      }
    }
    else if(result.isUserCancelled() == false) {
      showErrorMessageThreadSafe(result.getError(), activity.getResources().getString(R.string.error_message_could_not_download_app, appInfo.getTitle()));
    }
  }


  protected void setActionMenuInstallAppStateThreadSafe() {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setActionMenuInstallAppState();
      }
    });
  }

  protected void setActionMenuInstallAppState() {
    if(appInfo.getState() == AppState.INSTALLABLE) {
      setActionMenuInstallAppToStateInstallable();
    }
    else if(appInfo.getState() == AppState.REINSTALLABLE) {
      setActionMenuInstallAppToStateReinstallable();
    }
    else if(appInfo.getState() == AppState.UPDATABLE) {
      setActionMenuInstallAppToStateUpdatable();
    }
    else if(appInfo.getState() == AppState.GETTING_DOWNLOAD_URL) {
      setActionMenuInstallAppToStateGettingDownloadUrl();
    }
    else if(appInfo.getState() == AppState.DOWNLOADING) {
      setActionMenuInstallAppToStateDownloading();
    }
    else if(appInfo.getState() == AppState.VERIFYING_SIGNATURE) {
      setActionMenuInstallAppToStateVerifyingSignature();
    }
    else if(appInfo.getState() == AppState.INSTALLING) {
      setActionMenuInstallAppToStateInstalling();
    }
  }

  protected void setActionMenuInstallAppToStateInstallable() {
    if(txtvwInstallationStep != null) {
      txtvwInstallationStep.setText(R.string.install);
    }

    if(prgbrInstallationProgress != null) {
      prgbrInstallationProgress.setVisibility(View.GONE);
    }

    if(installAppActionView != null) {
      installAppActionView.setEnabled(true);
    }
  }

  protected void setActionMenuInstallAppToStateReinstallable() {
    if(txtvwInstallationStep != null) {
      txtvwInstallationStep.setText(R.string.reinstall);
    }

    if(prgbrInstallationProgress != null) {
      prgbrInstallationProgress.setVisibility(View.GONE);
    }

    if(installAppActionView != null) {
      installAppActionView.setEnabled(true);
    }
  }

  protected void setActionMenuInstallAppToStateUpdatable() {
    if(txtvwInstallationStep != null) {
      txtvwInstallationStep.setText(R.string.update);
    }

    if(prgbrInstallationProgress != null) {
      prgbrInstallationProgress.setVisibility(View.GONE);
    }

    if(installAppActionView != null) {
      installAppActionView.setEnabled(true);
    }
  }

  protected void setActionMenuInstallAppToStateGettingDownloadUrl() {
    txtvwInstallationStep.setText(R.string.get_download_url);

    prgbrInstallationProgress.setIndeterminate(true);
    prgbrInstallationProgress.setVisibility(View.VISIBLE);

    installAppActionView.setEnabled(false);
  }

  protected void setActionMenuInstallAppToStateDownloading() {
    txtvwInstallationStep.setText(R.string.downloading);

    prgbrInstallationProgress.setIndeterminate(true);
    prgbrInstallationProgress.setVisibility(View.VISIBLE);

    installAppActionView.setEnabled(false);
  }

  protected void setActionMenuInstallAppToStateVerifyingSignature() {
    txtvwInstallationStep.setText(R.string.verifying_downloaded_apk);

    prgbrInstallationProgress.setIndeterminate(true);
    prgbrInstallationProgress.setVisibility(View.VISIBLE);

    installAppActionView.setEnabled(false);
  }

  protected void setActionMenuInstallAppToStateInstalling() {
    txtvwInstallationStep.setText(R.string.installing);

    prgbrInstallationProgress.setIndeterminate(true);
    prgbrInstallationProgress.setVisibility(View.VISIBLE);

    installAppActionView.setEnabled(false);
  }


  protected void showErrorMessageThreadSafe(String error, String title) {
    AlertHelper.showErrorMessageThreadSafe(activity, error, title);
  }


  protected AppStateListener appStateListener = new AppStateListener() {
    @Override
    public void stateChanged(AppState newState, AppState previousState) {
      setActionMenuInstallAppStateThreadSafe();
    }
  };

}
