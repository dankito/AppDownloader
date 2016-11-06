package net.dankito.appdownloader.fragments;

import android.app.DownloadManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.dankito.appdownloader.AppDownloaderApplication;
import net.dankito.appdownloader.PlayStoreAppSearcher;
import net.dankito.appdownloader.R;
import net.dankito.appdownloader.adapter.AppSearchResultsAdapter;
import net.dankito.appdownloader.downloader.ApkDownloaderPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.EvoziPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.responses.AppSearchResult;
import net.dankito.appdownloader.responses.DownloadAppResponse;
import net.dankito.appdownloader.responses.GetAppDetailsResponse;
import net.dankito.appdownloader.responses.GetAppDownloadUrlResponse;
import net.dankito.appdownloader.responses.SearchAppsResponse;
import net.dankito.appdownloader.responses.callbacks.DownloadAppCallback;
import net.dankito.appdownloader.responses.callbacks.GetAppDetailsCallback;
import net.dankito.appdownloader.responses.callbacks.GetAppDownloadUrlResponseCallback;
import net.dankito.appdownloader.responses.callbacks.SearchAppsResponseCallback;
import net.dankito.appdownloader.util.AndroidOnUiThreadRunner;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.util.web.AndroidDownloadManager;
import net.dankito.appdownloader.util.web.IWebClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppSearchResultsFragment extends Fragment {

  private static final Logger log = LoggerFactory.getLogger(AppSearchResultsFragment.class);


  protected IOnUiThreadRunner uiThreadRunner;

  @Inject
  protected PlayStoreAppSearcher appSearcher;

  @Inject
  protected ApkDownloaderPlayStoreAppDownloader apkDownloaderPlayStoreAppDownloader;

  @Inject
  protected EvoziPlayStoreAppDownloader evoziPlayStoreAppDownloader;

  protected List<IAppDownloader> appDownloaders = new ArrayList<>();

  @Inject
  protected IWebClient webClient;

  protected AndroidDownloadManager downloadManager;

  protected AppSearchResultsAdapter searchResultsAdapter;


  public AppSearchResultsFragment() {
    this.uiThreadRunner = new AndroidOnUiThreadRunner(getActivity());

    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_app_search_results, container, false);

    injectComponents();

    searchResultsAdapter = new AppSearchResultsAdapter(getActivity());

    ListView lstvwAppSearchResults = (ListView)view.findViewById(R.id.lstvwAppSearchResults);
    lstvwAppSearchResults.setAdapter(searchResultsAdapter);
    lstvwAppSearchResults.setOnItemClickListener(lstvwAppSearchResultsItemClickListener);

    return view;
  }

  protected void injectComponents() {
    ((AppDownloaderApplication) getActivity().getApplication()).getComponent().inject(this);

    appSearcher.addRetrievedAppDetailsListener(appDetailsRetrievedListener);

    appDownloaders.add(apkDownloaderPlayStoreAppDownloader);
    appDownloaders.add(evoziPlayStoreAppDownloader);

    this.downloadManager = new AndroidDownloadManager(getActivity());

    //set filter to only when download is complete and register broadcast receiver
    IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
    getActivity().registerReceiver(downloadManager, filter);
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    MenuItem searchItem = menu.findItem(R.id.search);
    SearchView searchView = (SearchView) searchItem.getActionView();
    if(searchView != null) {
      SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);
      searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
      searchView.setQueryHint(getActivity().getString(R.string.search_hint_search_apps));
      searchView.setOnQueryTextListener(entriesQueryTextListener);
    }

    super.onCreateOptionsMenu(menu, inflater);
  }

  protected SearchView.OnQueryTextListener entriesQueryTextListener = new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
      searchApps(query);
      return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
//      searchApps(query);
      return true;
    }
  };

  protected void searchApps(String query) {
    appSearcher.searchAsync(query, new SearchAppsResponseCallback() {
      @Override
      public void completed(SearchAppsResponse response) {
        if(response.isSuccessful()) {
          appSearchResultRetrieved(response.getAppSearchResults());
        }
        else {
          showErrorMessageThreadSafe(getString(R.string.error_message_could_not_search_for_apps, response.getError()));
        }
      }
    });
  }

  protected void appSearchResultRetrieved(final List<AppSearchResult> appSearchResults) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        searchResultsAdapter.setAppSearchResults(appSearchResults);
      }
    });
  }


  protected AdapterView.OnItemClickListener lstvwAppSearchResultsItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      final AppSearchResult clickedApp = (AppSearchResult)view.getTag();

      if(clickedApp.isAlreadyDownloaded() == false) {
        if(clickedApp.hasDownloadUrls()) {
          for(String appDownloadUrl : clickedApp.getDownloadUrls()) {
            downloadApp(clickedApp, appDownloadUrl);
          }
        }
        else {
          getAppDownloadLinkAndDownloadApp(clickedApp);
        }
      }
    }
  };

  protected void getAppDownloadLinkAndDownloadApp(final AppSearchResult clickedApp) {
    final AtomicBoolean hasDownloadUrlBeenRetrieved = new AtomicBoolean(false);

    for(IAppDownloader appDownloader : appDownloaders) {
      appDownloader.getAppDownloadLinkAsync(clickedApp, new GetAppDownloadUrlResponseCallback() {
        @Override
        public void completed(GetAppDownloadUrlResponse response) {
          synchronized(hasDownloadUrlBeenRetrieved) {
            getAppDownloadLinkCompleted(clickedApp, response, hasDownloadUrlBeenRetrieved);
          }
        }
      });
    }
  }

  protected void getAppDownloadLinkCompleted(AppSearchResult clickedApp, GetAppDownloadUrlResponse response, AtomicBoolean hasDownloadUrlBeenRetrieved) {
    if(response.isSuccessful()) {
      clickedApp.addDownloadUrl(response.getUrl());
    }

    if(hasDownloadUrlBeenRetrieved.get() == false) {
      if(response.isSuccessful() == false) {
        showErrorMessageThreadSafe(getString(R.string.error_message_could_not_download_app, response.getError()));
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

//    downloadAppBySelectingAppToDownloadViaIntent(appDownloadUrl);
//
//    downloadAppViaAppDownloader(clickedApp);
  }

  protected void downloadAppViaAndroidDownloadManager(AppSearchResult clickedApp, String appDownloadUrl) {
    downloadManager.downloadUrlAsync(clickedApp, appDownloadUrl);
  }

  protected void downloadAppBySelectingAppToDownloadViaIntent(String appDownloadUrl) {
    try {
      Intent intent = new Intent();
      intent.setAction(Intent.ACTION_VIEW);
      intent.setData(Uri.parse(appDownloadUrl));
      startActivityForResult(intent, 10);
    } catch(Exception e) { showErrorMessageThreadSafe("Could not parse App Download Link " + appDownloadUrl + ": " + e.getLocalizedMessage()); }
  }

  protected void downloadAppViaAppDownloader(final AppSearchResult clickedApp) {
    apkDownloaderPlayStoreAppDownloader.downloadAppAsync(clickedApp, new DownloadAppCallback() {
      @Override
      public void completed(DownloadAppResponse response) {
        appDownloadCompleted(clickedApp, response);
      }
    });
  }

  protected void appDownloadCompleted(AppSearchResult clickedApp, DownloadAppResponse response) {
    if(response.isSuccessful() == false) {
      showErrorMessageThreadSafe(getString(R.string.error_message_could_not_download_app, response.getError()));
    }
    else {
      clickedApp.setDownloadLocation(response.getDownloadLocation());

      Intent intent = new Intent();
      intent.setAction(android.content.Intent.ACTION_VIEW);
      intent.setDataAndType(Uri.fromFile(response.getDownloadLocation()), "application/vnd.android.package-archive");
      startActivityForResult(intent, 10);
    }
  }

  protected GetAppDetailsCallback appDetailsRetrievedListener = new GetAppDetailsCallback() {
    @Override
    public void completed(GetAppDetailsResponse response) {
      getActivity().runOnUiThread(new Runnable() {
        @Override
        public void run() {
          searchResultsAdapter.notifyDataSetChanged();
        }
      });
    }
  };


  protected void showErrorMessageThreadSafe(final String errorMessage) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showErrorMessage(errorMessage);
      }
    });
  }

  protected void showErrorMessage(String errorMessage) {
//    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();

    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder = builder.setMessage(errorMessage);

    builder.setNegativeButton(R.string.ok, null);

    builder.create().show();
  }

}
