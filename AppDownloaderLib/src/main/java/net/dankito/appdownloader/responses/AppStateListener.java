package net.dankito.appdownloader.responses;

/**
 * Created by ganymed on 14/11/16.
 */

public interface AppStateListener {

  void stateChanged(AppState newState, AppState previousState);

}
