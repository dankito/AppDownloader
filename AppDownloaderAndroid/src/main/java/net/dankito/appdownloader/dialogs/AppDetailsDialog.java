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
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.responses.AppSearchResult;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.util.AlertHelper;
import net.dankito.appdownloader.util.web.AndroidDownloadManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ganymed on 06/11/16.
 */

public class AppDetailsDialog extends FullscreenDialog {

  private static final Logger log = LoggerFactory.getLogger(AppDetailsDialog.class);


  protected AppSearchResult appSearchResult;

  protected List<IAppDownloader> appDownloaders;

  protected AndroidDownloadManager downloadManager;

  protected WebView wbvwViewAppDetails;


  public AppDetailsDialog(AppCompatActivity context) {
    super(context);

    this.downloadManager = new AndroidDownloadManager(context);
  }


  public void setAppSearchResult(AppSearchResult appSearchResult) {
    this.appSearchResult = appSearchResult;
  }

  public void setAppDownloaders(List<IAppDownloader> appDownloaders) {
    this.appDownloaders = appDownloaders;
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
//    toolbar.setTitle(appSearchResult.getTitle());

    wbvwViewAppDetails.loadUrl(appSearchResult.getAppDetailsPageUrl());

    invalidateOptionsMenu();
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
    View actionView = installAppItem.getActionView();

    ProgressBar prgbrInstallationProgress = (ProgressBar)actionView.findViewById(R.id.prgbrInstallationProgress);
    prgbrInstallationProgress.setIndeterminate(true);

    actionView.setOnClickListener(mnitmInstallAppClickListener);

    return super.onCreateOptionsMenu(menu);
  }

  protected View.OnClickListener mnitmInstallAppClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      mnitmInstallAppClicked(view);
    }
  };

  protected void mnitmInstallAppClicked(View actionView) {
    TextView txtvwInstallationStep = (TextView)actionView.findViewById(R.id.txtvwInstallationStep);
    txtvwInstallationStep.setText("Getting Download Url");
    ProgressBar prgbrInstallationProgress = (ProgressBar)actionView.findViewById(R.id.prgbrInstallationProgress);
    prgbrInstallationProgress.setIndeterminate(true);
    prgbrInstallationProgress.setVisibility(View.VISIBLE);

    installApp();
  }

  protected void installApp() {
    if(appSearchResult.isAlreadyDownloaded() == false) {
      if(appSearchResult.hasDownloadUrls()) {
        downloadApp(appSearchResult, getBestAppDownloadUrl(appSearchResult));
      }
      else {
        getAppDownloadLinkAndDownloadApp(appSearchResult);
      }
    }
  }

  protected String getBestAppDownloadUrl(AppSearchResult appSearchResult) {
    if(appSearchResult.getDownloadUrls().size() == 1) {
      return appSearchResult.getDownloadUrls().get(0);
    }
    else {
      // TODO: choose best one
      return appSearchResult.getDownloadUrls().get(0);
    }
  }

  protected void getAppDownloadLinkAndDownloadApp(final AppSearchResult clickedApp) {
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

  protected void getAppDownloadLinkCompleted(AppSearchResult clickedApp, GetAppDownloadUrlResponse response, AtomicBoolean hasDownloadUrlBeenRetrieved, AtomicInteger countRequestsAppDownloadLinkCompleted) {
    countRequestsAppDownloadLinkCompleted.incrementAndGet();

    if(response.isSuccessful()) {
      clickedApp.addDownloadUrl(response.getUrl());
    }

    if(hasDownloadUrlBeenRetrieved.get() == false) {
      if(response.isSuccessful() == false) {
        if(countRequestsAppDownloadLinkCompleted.get() == appDownloaders.size()) { // only show error message if it's been the last AppDownloader which's request completed
          AlertHelper.showErrorMessageThreadSafe(activity, activity.getString(R.string.error_message_could_not_download_app, response.getError()));
        }
      }
      else {
        downloadApp(clickedApp, response.getUrl());
      }
    }

    if(response.isSuccessful()) {
      hasDownloadUrlBeenRetrieved.set(true);
    }
  }

  protected void downloadApp(AppSearchResult clickedApp, String appDownloadUrl) {
    log.info("Starting to download App " + clickedApp + " from " + appDownloadUrl + " ...");

    downloadAppViaAndroidDownloadManager(clickedApp, appDownloadUrl);
  }

  protected void downloadAppViaAndroidDownloadManager(AppSearchResult clickedApp, String appDownloadUrl) {
    downloadManager.downloadUrlAsync(clickedApp, appDownloadUrl);
  }

}
