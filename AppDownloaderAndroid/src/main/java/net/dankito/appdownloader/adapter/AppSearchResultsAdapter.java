package net.dankito.appdownloader.adapter;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.app.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class AppSearchResultsAdapter extends BaseAdapter {

  protected Activity activity;

  protected List<AppInfo> searchResults = new ArrayList<>(0);


  public AppSearchResultsAdapter(Activity activity) {
    this.activity = activity;
  }


  public void setSearchResults(List<AppInfo> searchResults) {
    this.searchResults = searchResults;

    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return searchResults.size();
  }

  @Override
  public Object getItem(int index) {
    return searchResults.get(index);
  }

  @Override
  public long getItemId(int index) {
    return index;
  }

  @Override
  public View getView(int index, View convertView, ViewGroup parent) {
    if(convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.list_item_app_search_result, parent, false);
    }

    AppInfo appInfo = (AppInfo)getItem(index);

    TextView txtvwAppTitle = (TextView)convertView.findViewById(R.id.txtvwAppTitle);
    txtvwAppTitle.setText(appInfo.getTitle());

    TextView txtvwAppDeveloper = (TextView)convertView.findViewById(R.id.txtvwAppDeveloper);
    txtvwAppDeveloper.setText(appInfo.getDeveloper());

    TextView txtvwRatings = (TextView)convertView.findViewById(R.id.txtvwAppRatings);
    if(appInfo.areAppDetailsDownloaded()) {
      txtvwRatings.setText(appInfo.getRating() + " (" + appInfo.getCountRatings() + ")");
    }
    else {
      txtvwRatings.setText("");
    }

    TextView txtvwVersion = (TextView)convertView.findViewById(R.id.txtvwAppVersion);
    if(appInfo.areAppDetailsDownloaded()) {
      txtvwVersion.setText(appInfo.getVersion() + " (" + appInfo.getCountInstallations() + ")");
    }
    else {
      txtvwVersion.setText("");
    }

    ImageView imgvwAppIcon = (ImageView)convertView.findViewById(R.id.imgvwAppIcon);
    if(appInfo.hasSmallCoverImageUrl()) {
      imgvwAppIcon.setVisibility(View.VISIBLE);
      Picasso.with(activity)
          .load(appInfo.getSmallCoverImageUrl())
          .into(imgvwAppIcon);
    }
    else if(appInfo.getIconImage() instanceof Drawable) {
      imgvwAppIcon.setVisibility(View.VISIBLE);
      imgvwAppIcon.setImageDrawable((Drawable)appInfo.getIconImage());
    }
    else {
      imgvwAppIcon.setVisibility(View.INVISIBLE);
    }

    convertView.setTag(appInfo);

    return convertView;
  }
}
