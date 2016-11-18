package net.dankito.appdownloader.util.android;

import android.content.Intent;

/**
 * Created by ganymed on 19/11/16.
 */

public interface IActivityResultListener {

  void receivedActivityResult(int requestCode, int resultCode, Intent data);

}
