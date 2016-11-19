package net.dankito.appdownloader.adapter;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.fragments.AppSearchResultsFragment;

/**
 * Created by ganymed on 19/11/16.
 */

public class ActivityMainTabsAdapter extends FragmentPagerAdapter {

  protected Activity activity;

  protected FragmentManager fragmentManager;


  protected AppSearchResultsFragment appSearchResultsFragment = null;


  public ActivityMainTabsAdapter(AppCompatActivity activity) {
    super(activity.getSupportFragmentManager());

    this.activity = activity;
    this.fragmentManager = activity.getSupportFragmentManager();
  }


  @Override
  public int getCount() {
    return 1;
  }

  @Override
  public CharSequence getPageTitle(int position) {
    if(position == 0) {
      return activity.getString(R.string.tab_title_search);
    }

    return super.getPageTitle(position);
  }

  @Override
  public Fragment getItem(int position) {
    if(position == 0) {
      if(appSearchResultsFragment == null) {
        appSearchResultsFragment = new AppSearchResultsFragment();
      }

      return appSearchResultsFragment;
    }

    return null;
  }
}
