package net.dankito.appdownloader.util;

import android.app.Activity;
import android.support.v7.app.AlertDialog;

import net.dankito.appdownloader.R;

/**
 * Created by ganymed on 14/11/16.
 */

public class AlertHelper {

  public static void showErrorMessageThreadSafe(final Activity activity, final String errorMessage) {
    activity.runOnUiThread(new Runnable() {
      @Override
      public void run() {
        showErrorMessage(activity, errorMessage);
      }
    });
  }

  public static void showErrorMessage(Activity activity, String errorMessage) {
    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
    builder = builder.setMessage(errorMessage);

    builder.setNegativeButton(R.string.ok, null);

    builder.create().show();
  }

}
