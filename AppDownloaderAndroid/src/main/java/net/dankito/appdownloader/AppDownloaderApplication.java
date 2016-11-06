package net.dankito.appdownloader;

import android.app.Application;

import net.dankito.appdownloader.di.AndroidDiComponent;
import net.dankito.appdownloader.di.DaggerAndroidDiComponent;

/**
 * Created by ganymed on 03/11/16.
 */

public class AppDownloaderApplication extends Application {

  AndroidDiComponent component;

  @Override
  public void onCreate() {
    super.onCreate();
    component = DaggerAndroidDiComponent.builder().build();
  }

  public AndroidDiComponent getComponent() {
    return component;
  }

}
