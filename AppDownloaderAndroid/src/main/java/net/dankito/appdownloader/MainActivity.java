package net.dankito.appdownloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.dankito.appdownloader.di.AndroidDiComponent;
import net.dankito.appdownloader.di.AndroidDiContainer;
import net.dankito.appdownloader.di.DaggerAndroidDiComponent;
import net.dankito.appdownloader.util.android.IActivityResultListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {


  protected AndroidDiComponent component;

  protected Map<Integer, IActivityResultListener> activityResultListeners = new ConcurrentHashMap<>();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupDependencyInjection();

    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
  }

  protected void setupDependencyInjection() {
    component = DaggerAndroidDiComponent.builder()
                .androidDiContainer(new AndroidDiContainer(this))
                .build();

    component.inject(this);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    IActivityResultListener activityResultListener = activityResultListeners.get(requestCode);

    if(activityResultListener != null) {
      activityResultListener.receivedActivityResult(requestCode, resultCode, data);
    }
    else {
      super.onActivityResult(requestCode, resultCode, data);
    }
  }


  public void registerActivityResultListener(int requestCode, IActivityResultListener activityResultListener) {
    activityResultListeners.put(requestCode, activityResultListener);
  }

  public void unregisterActivityResultListener(int requestCode) {
    activityResultListeners.remove(requestCode);
  }


  public AndroidDiComponent getComponent() {
    return component;
  }

}
