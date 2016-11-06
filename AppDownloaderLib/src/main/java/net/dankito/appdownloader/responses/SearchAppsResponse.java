package net.dankito.appdownloader.responses;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class SearchAppsResponse extends ResponseBase {

  protected List<AppSearchResult> appSearchResults = new ArrayList<>(0);


  public SearchAppsResponse(String error) {
    super(error);
  }

  public SearchAppsResponse(List<AppSearchResult> appSearchResults) {
    super(true);
    this.appSearchResults = appSearchResults;
  }


  public List<AppSearchResult> getAppSearchResults() {
    return appSearchResults;
  }


  @Override
  public String toString() {
    if(isSuccessful()) {
      return "Found " + appSearchResults.size() + " Apps";
    }

    return super.toString();
  }

}
