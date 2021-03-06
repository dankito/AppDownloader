package net.dankito.appdownloader.fragments;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import net.dankito.appdownloader.IPlayStoreAppSearcher;
import net.dankito.appdownloader.MainActivity;
import net.dankito.appdownloader.R;
import net.dankito.appdownloader.adapter.AppSearchResultsAdapter;
import net.dankito.appdownloader.app.model.AppInfo;
import net.dankito.appdownloader.dialogs.AppDetailsDialog;
import net.dankito.appdownloader.downloader.ApkDownloaderPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.ApkMirrorPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.EvoziPlayStoreAppDownloader;
import net.dankito.appdownloader.downloader.IAppDownloader;
import net.dankito.appdownloader.responses.SearchAppsResponse;
import net.dankito.appdownloader.responses.callbacks.SearchAppsResponseCallback;
import net.dankito.appdownloader.util.AlertHelper;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.util.web.IWebClient;

import java.util.List;

import javax.inject.Inject;

/**
 * A placeholder fragment containing a simple view.
 */
public class AppSearchResultsFragment extends Fragment {

  @Inject
  protected IOnUiThreadRunner uiThreadRunner;

  @Inject
  protected IPlayStoreAppSearcher appSearcher;

  @Inject
  protected ApkMirrorPlayStoreAppDownloader apkMirrorPlayStoreAppDownloader;

  @Inject
  protected ApkDownloaderPlayStoreAppDownloader apkDownloaderPlayStoreAppDownloader;

  @Inject
  protected EvoziPlayStoreAppDownloader evoziPlayStoreAppDownloader;

  @Inject
  protected List<IAppDownloader> appDownloaders;

  @Inject
  protected IWebClient webClient;

  protected AppSearchResultsAdapter searchResultsAdapter;

  protected SearchView searchView = null;

  protected AppDetailsDialog appDetailsDialog = null;


  public AppSearchResultsFragment() {
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_app_search_results, container, false);

    injectComponents();

    searchResultsAdapter = new AppSearchResultsAdapter(getActivity(), appSearcher);

    ListView lstvwAppSearchResults = (ListView)view.findViewById(R.id.lstvwAppSearchResults);
    lstvwAppSearchResults.setAdapter(searchResultsAdapter);
    lstvwAppSearchResults.setOnItemClickListener(lstvwAppSearchResultsItemClickListener);

    FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.fabSearchApps);
    fab.setOnClickListener(floatingActionButtonSearchOnClickListener);

    return view;
  }

  protected void injectComponents() {
    ((MainActivity) getActivity()).getComponent().inject(this);
  }


  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    if(isAppDetailsDialogShowing()) {
      appDetailsDialog.onCreateOptionsMenu(menu);
    }
    else {
      inflater.inflate(R.menu.menu_app_search_results_fragment, menu);

      MenuItem searchMenuItem = menu.findItem(R.id.search);
      searchView = (SearchView) searchMenuItem.getActionView();
      if(searchView != null) {
        FragmentActivity activity = getActivity();
        if(activity != null) {
          SearchManager searchManager = (SearchManager) activity.getSystemService(Context.SEARCH_SERVICE);
          searchView.setSearchableInfo(searchManager.getSearchableInfo(activity.getComponentName()));
          searchView.setQueryHint(getActivity().getString(R.string.search_hint_search_apps));
          searchView.setOnQueryTextListener(entriesQueryTextListener);
        }
      }

      super.onCreateOptionsMenu(menu, inflater);
    }
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    if(isAppDetailsDialogShowing()) {
      appDetailsDialog.onPrepareOptionsMenu(menu);
    }
    else {
      super.onPrepareOptionsMenu(menu);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if(isAppDetailsDialogShowing()) {
      return appDetailsDialog.onOptionsItemSelected(item);
    }
    else {
      return super.onOptionsItemSelected(item);
    }
  }

  protected boolean isAppDetailsDialogShowing() {
    return appDetailsDialog != null && appDetailsDialog.isShowing();
  }

  protected SearchView.OnQueryTextListener entriesQueryTextListener = new SearchView.OnQueryTextListener() {
    @Override
    public boolean onQueryTextSubmit(String query) {
      searchApps(query);
      return true;
    }

    @Override
    public boolean onQueryTextChange(String query) {
      return true;
    }
  };

  protected void searchApps(String query) {
    appSearcher.searchAsync(query, new SearchAppsResponseCallback() {
      @Override
      public void searchResultsRetrieved(SearchAppsResponse response) {
        if(response.isSuccessful()) {
          appSearchResultRetrieved(response.getSearchResults());
        }
        else {
          AlertHelper.showErrorMessageThreadSafe(getActivity(), getString(R.string.error_message_could_not_search_for_apps, response.getError()));
        }
      }
    });
  }

  protected void appSearchResultRetrieved(final List<AppInfo> searchResults) {
    getActivity().runOnUiThread(new Runnable() {
      @Override
      public void run() {
        searchResultsAdapter.setSearchResults(searchResults);
      }
    });
  }


  protected AdapterView.OnItemClickListener lstvwAppSearchResultsItemClickListener = new AdapterView.OnItemClickListener() {
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
      final AppInfo clickedApp = (AppInfo)view.getTag();

      appSearchResultSelected(clickedApp);
    }
  };

  protected void appSearchResultSelected(AppInfo clickedApp) {
    AppDetailsDialog appDetailsDialog = getAppDetailsDialog();

    appDetailsDialog.setAppInfo(clickedApp);
    appDetailsDialog.setAppDownloaders(appDownloaders);

    appDetailsDialog.show();
  }

  protected AppDetailsDialog getAppDetailsDialog() {
    if(appDetailsDialog == null) {
      appDetailsDialog = createAppDetailsDialog();
    }

    return appDetailsDialog;
  }

  protected AppDetailsDialog createAppDetailsDialog() {
    AppDetailsDialog appDetailsDialog = new AppDetailsDialog((AppCompatActivity)getActivity());

    return appDetailsDialog;
  }


  protected View.OnClickListener floatingActionButtonSearchOnClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      if(searchView != null) {
        searchView.setIconified(false);
      }
    }
  };

}
