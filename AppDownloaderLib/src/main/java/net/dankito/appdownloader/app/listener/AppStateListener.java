package net.dankito.appdownloader.app.listener;

import net.dankito.appdownloader.app.model.AppState;

/**
 * Created by ganymed on 14/11/16.
 */

public interface AppStateListener {

  void stateChanged(AppState newState, AppState previousState);

}
