package net.dankito.appdownloader.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import net.dankito.appdownloader.MainActivity;
import net.dankito.appdownloader.R;
import net.dankito.appdownloader.adapter.UpdatableAppsAdapter;
import net.dankito.appdownloader.app.IUpdatableAppsManager;

import javax.inject.Inject;

/**
 * Created by ganymed on 19/11/16.
 */

public class UpdatableAppsFragment extends Fragment {

  @Inject
  protected IUpdatableAppsManager updatableAppsManager;

  protected UpdatableAppsAdapter updatableAppsAdapter;


  public UpdatableAppsFragment() {
    setHasOptionsMenu(false);
  }


  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_updatable_apps, container, false);

    injectComponents();

    updatableAppsAdapter = new UpdatableAppsAdapter(getActivity());
    updatableAppsAdapter.setUpdatableAppsManager(updatableAppsManager);

    ListView lstvwUpdatableApps = (ListView)view.findViewById(R.id.lstvwUpdatableApps);
    lstvwUpdatableApps.setAdapter(updatableAppsAdapter);

    return view;
  }

  protected void injectComponents() {
    ((MainActivity) getActivity()).getComponent().inject(this);
  }
}
