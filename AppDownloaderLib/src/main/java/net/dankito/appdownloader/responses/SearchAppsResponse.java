package net.dankito.appdownloader.responses;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ganymed on 02/11/16.
 */

public class SearchAppsResponse extends ResponseBase {

  protected List<AppInfo> appInfos = new ArrayList<>(0);


  public SearchAppsResponse(String error) {
    super(error);
  }

  public SearchAppsResponse(List<AppInfo> appInfos) {
    super(true);
    this.appInfos = appInfos;
  }


  public List<AppInfo> getAppInfos() {
    return appInfos;
  }


  @Override
  public String toString() {
    if(isSuccessful()) {
      return "Found " + appInfos.size() + " Apps";
    }

    return super.toString();
  }

}
