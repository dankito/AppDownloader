package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 14/11/16.
 */

public interface AppSearchResultStateListener {

  void stateChanged(AppSearchResultState newState, AppSearchResultState previousState);

}
