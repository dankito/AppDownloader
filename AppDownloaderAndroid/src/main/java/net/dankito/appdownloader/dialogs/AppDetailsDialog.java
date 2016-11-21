package net.dankito.appdownloader.dialogs;

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
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.app.model.AppState;
import net.dankito.appdownloader.app.listener.AppStateListener;
import net.dankito.appdownloader.app.IAppDownloadAndInstallationService;
import net.dankito.appdownloader.app.IAppInstaller;
import net.dankito.appdownloader.app.IAppVerifier;
import net.dankito.appdownloader.di.AndroidDiComponent;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.util.web.IDownloadManager;

import java.util.List;

import javax.inject.Inject;

/**
 * Created by ganymed on 06/11/16.
 */

public class AppDetailsDialog extends FullscreenDialog {

  protected AppInfo appInfo;

  protected List<IAppDownloader> appDownloaders;

  @Inject
  protected IDownloadManager downloadManager;

  @Inject
  protected IAppVerifier appVerifier;

  @Inject
  protected IAppInstaller appInstaller;

  @Inject
  protected IAppDownloadAndInstallationService downloadAndInstallationService;

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
    downloadAndInstallationService.installApp(appInfo);
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



  protected AppStateListener appStateListener = new AppStateListener() {
    @Override
    public void stateChanged(AppState newState, AppState previousState) {
      setActionMenuInstallAppStateThreadSafe();
    }
  };

}
