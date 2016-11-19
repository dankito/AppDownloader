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
import net.dankito.appdownloader.app.IInstalledAppsManager;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by ganymed on 19/11/16.
 */

public class InstalledAppsAdapter extends BaseAdapter {

  protected Activity activity;

  protected IInstalledAppsManager installedAppsManager;

  protected List<AppInfo> installedApps;



  public InstalledAppsAdapter(Activity activity) {
    this.activity = activity;
  }


  public void setInstalledAppsManager(IInstalledAppsManager installedAppsManager) {
    this.installedAppsManager = installedAppsManager;

    setInstalledApps(installedAppsManager.getAllInstalledApps());
  }

  protected void setInstalledApps(List<AppInfo> installedApps) {
    Collections.sort(installedApps, installedAppsComparator);
    this.installedApps = installedApps;

    notifyDataSetChanged();
  }


  @Override
  public int getCount() {
    return installedApps.size();
  }

  @Override
  public Object getItem(int index) {
    return installedApps.get(index);
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.list_item_installed_app, parent, false);
    }

    AppInfo appInfo = (AppInfo)getItem(index);

    TextView txtvwAppTitle = (TextView)convertView.findViewById(R.id.txtvwAppTitle);
    txtvwAppTitle.setText(appInfo.getTitle());

    TextView txtvwVersion = (TextView)convertView.findViewById(R.id.txtvwAppVersion);
    txtvwVersion.setText(appInfo.getInstalledVersion());

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


  protected Comparator<AppInfo> installedAppsComparator = new Comparator<AppInfo>() {
    @Override
    public int compare(AppInfo appInfo1, AppInfo appInfo2) {
      return appInfo1.getTitle().compareTo(appInfo2.getTitle());
    }
  };

}
