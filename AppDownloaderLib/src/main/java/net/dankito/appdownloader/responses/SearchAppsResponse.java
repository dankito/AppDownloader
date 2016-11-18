package net.dankito.appdownloader.responses;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class SearchAppsResponse extends ResponseBase {

  protected List<AppInfo> searchResults = new ArrayList<>(0);


  public SearchAppsResponse(String error) {
    super(error);
  }

  public SearchAppsResponse(List<AppInfo> searchResults) {
    super(true);
    this.searchResults = searchResults;
  }


  public List<AppInfo> getSearchResults() {
    return searchResults;
  }


  @Override
  public String toString() {
    if(isSuccessful()) {
      return "Found " + searchResults.size() + " Apps";
    }

    return super.toString();
  }

}
