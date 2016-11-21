package net.dankito.appdownloader.responses;

import net.dankito.appdownloader.app.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class SearchAppsResponse extends ResponseBase {

  protected List<AppInfo> searchResults = new ArrayList<>(0);

  protected boolean hasCompleted = false;


  public SearchAppsResponse(String error) {
    super(error);
  }

  public SearchAppsResponse(List<AppInfo> searchResults, boolean hasCompleted) {
    super(true);
    this.searchResults = searchResults;
    this.hasCompleted = hasCompleted;
  }


  public List<AppInfo> getSearchResults() {
    return searchResults;
  }

  public boolean isHasCompleted() {
    return hasCompleted;
  }

  @Override
  public String toString() {
    if(isSuccessful()) {
      return "Found " + searchResults.size() + " Apps";
    }

    return super.toString();
  }

}
