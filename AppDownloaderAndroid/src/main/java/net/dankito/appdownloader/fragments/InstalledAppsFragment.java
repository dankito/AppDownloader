package net.dankito.appdownloader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.dankito.appdownloader.MainActivity;
import net.dankito.appdownloader.R;
import net.dankito.appdownloader.adapter.InstalledAppsAdapter;
import net.dankito.appdownloader.util.IOnUiThreadRunner;
import net.dankito.appdownloader.app.IInstalledAppsManager;

import javax.inject.Inject;

/**
 * Created by ganymed on 19/11/16.
 */

public class InstalledAppsFragment extends Fragment {

  @Inject
  protected IInstalledAppsManager installedAppsManager;

  @Inject
  protected IOnUiThreadRunner uiThreadRunner;

  protected InstalledAppsAdapter installedAppsAdapter;


  public InstalledAppsFragment() {
    setHasOptionsMenu(false);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_installed_apps, container, false);

    injectComponents();

    installedAppsAdapter = new InstalledAppsAdapter(getActivity());
    installedAppsAdapter.setInstalledAppsManager(installedAppsManager);

    ListView lstvwInstalledApps = (ListView)view.findViewById(R.id.lstvwInstalledApps);
    lstvwInstalledApps.setAdapter(installedAppsAdapter);

    return view;
  }

  protected void injectComponents() {
    ((MainActivity) getActivity()).getComponent().inject(this);
  }
}
