package net.dankito.appdownloader.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.AppInfo;
import net.dankito.appdownloader.app.GetUpdatableAppsCallback;
import net.dankito.appdownloader.app.IUpdatableAppsManager;
import net.dankito.appdownloader.app.UpdatableAppsListener;
import net.dankito.appdownloader.app.UpdatableAppsListenerInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class UpdatableAppsAdapter extends BaseAdapter {

  protected Activity activity;

  protected IUpdatableAppsManager updatableAppsManager;

  protected List<AppInfo> updatableApps = new ArrayList<>();



  public UpdatableAppsAdapter(Activity activity) {
    this.activity = activity;
  }


  public void setUpdatableAppsManager(IUpdatableAppsManager updatableAppsManager) {
    this.updatableAppsManager = updatableAppsManager;

    updatableAppsManager.addUpdatableAppsListener(updatableAppsListener);

    retrieveUpdatableApps(updatableAppsManager);
  }

  protected void retrieveUpdatableApps(IUpdatableAppsManager updatableAppsManager) {
    updatableAppsManager.getUpdatableAppsAsync(new GetUpdatableAppsCallback() {
      @Override
      public void completed(List<AppInfo> updatableApps) {
        setUpdatableAppsThreadSafe(updatableApps);
      }
    });
  }

  protected void setUpdatableAppsThreadSafe(final List<AppInfo> updatableApps) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        setUpdatableApps(updatableApps);
      }
    });
  }

  protected void setUpdatableApps(List<AppInfo> updatableApps) {
    Collections.sort(updatableApps, updatableAppsComparator);
    this.updatableApps = updatableApps;

    notifyDataSetChanged();
  }


  @Override
  public int getCount() {
    return updatableApps.size();
  }

  @Override
  public Object getItem(int index) {
    return updatableApps.get(index);
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.list_item_updatable_app, parent, false);
    }

    AppInfo appInfo = (AppInfo)getItem(index);

    TextView txtvwAppTitle = (TextView)convertView.findViewById(R.id.txtvwAppTitle);
    txtvwAppTitle.setText(appInfo.getTitle());

    TextView txtvwVersion = (TextView)convertView.findViewById(R.id.txtvwAppVersion);
    txtvwVersion.setText(activity.getString(R.string.updatable_app_version_comparison, appInfo.getInstalledVersionString(), appInfo.getVersion()));

    ImageView imgvwAppIcon = (ImageView)convertView.findViewById(R.id.imgvwAppIcon);
    if(appInfo.getIconImage() instanceof Drawable) {
      imgvwAppIcon.setVisibility(View.VISIBLE);
      imgvwAppIcon.setImageDrawable((Drawable)appInfo.getIconImage());
    }
    else {
      imgvwAppIcon.setVisibility(View.INVISIBLE);
    }

    convertView.setTag(appInfo);

    return convertView;
  }


  protected Comparator<AppInfo> updatableAppsComparator = new Comparator<AppInfo>() {
    @Override
    public int compare(AppInfo appInfo1, AppInfo appInfo2) {
      return appInfo1.getTitle().compareTo(appInfo2.getTitle());
    }
  };


  protected UpdatableAppsListener updatableAppsListener = new UpdatableAppsListener() {
    @Override
    public void foundUpdatableApp(UpdatableAppsListenerInfo info) {
      setUpdatableAppsThreadSafe(info.getAllUpdatableApps());
    }

    @Override
    public void appUpdated(UpdatableAppsListenerInfo info) {
      setUpdatableAppsThreadSafe(info.getAllUpdatableApps());
    }
  };

}
