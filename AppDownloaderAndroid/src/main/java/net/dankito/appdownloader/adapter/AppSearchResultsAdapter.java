package net.dankito.appdownloader.adapter;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import net.dankito.appdownloader.R;
import net.dankito.appdownloader.responses.AppSearchResult;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class AppSearchResultsAdapter extends BaseAdapter {

  protected Activity activity;

  protected List<AppSearchResult> appSearchResults = new ArrayList<>(0);


  public AppSearchResultsAdapter(Activity activity) {
    this.activity = activity;
  }


  public void setAppSearchResults(List<AppSearchResult> appSearchResults) {
    this.appSearchResults = appSearchResults;

    notifyDataSetChanged();
  }

  @Override
  public int getCount() {
    return appSearchResults.size();
  }

  @Override
  public Object getItem(int index) {
    return appSearchResults.get(index);
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

    AppSearchResult appSearchResult = (AppSearchResult)getItem(index);

    TextView txtvwAppTitle = (TextView)convertView.findViewById(R.id.txtvwAppTitle);
    txtvwAppTitle.setText(appSearchResult.getTitle());

    TextView txtvwAppDeveloper = (TextView)convertView.findViewById(R.id.txtvwAppDeveloper);
    txtvwAppDeveloper.setText(appSearchResult.getDeveloper());

    TextView txtvwRatings = (TextView)convertView.findViewById(R.id.txtvwAppRatings);
    if(appSearchResult.areAppDetailsDownloaded()) {
      txtvwRatings.setText(appSearchResult.getRating() + " (" + appSearchResult.getCountRatings() + ")");
    }
    else {
      txtvwRatings.setText("");
    }

    TextView txtvwVersion = (TextView)convertView.findViewById(R.id.txtvwAppVersion);
    if(appSearchResult.areAppDetailsDownloaded()) {
      txtvwVersion.setText(appSearchResult.getVersion() + " (" + appSearchResult.getCountInstallations() + ")");
    }
    else {
      txtvwVersion.setText("");
    }

    ImageView imgvwAppIcon = (ImageView)convertView.findViewById(R.id.imgvwAppIcon);
    if(appSearchResult.hasSmallCoverImageUrl()) {
      imgvwAppIcon.setVisibility(View.VISIBLE);
      Picasso.with(activity)
          .load(appSearchResult.getSmallCoverImageUrl())
          .into(imgvwAppIcon);
    }
    else {
      imgvwAppIcon.setVisibility(View.INVISIBLE);
    }

    convertView.setTag(appSearchResult);

    return convertView;
  }
}
