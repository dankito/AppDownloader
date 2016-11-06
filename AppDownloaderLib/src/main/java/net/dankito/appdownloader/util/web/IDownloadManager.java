package net.dankito.appdownloader.util.web;

import net.dankito.appdownloader.responses.AppSearchResult;

/**
 * Created by ganymed on 05/11/16.
 */

public interface IDownloadManager {

  void downloadUrlAsync(AppSearchResult appSearchResult, String url);

}
