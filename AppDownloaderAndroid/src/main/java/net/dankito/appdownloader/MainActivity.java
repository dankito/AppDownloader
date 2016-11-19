package net.dankito.appdownloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.dankito.appdownloader.adapter.ActivityMainTabsAdapter;
import net.dankito.appdownloader.di.AndroidDiComponent;
import net.dankito.appdownloader.di.AndroidDiContainer;
import net.dankito.appdownloader.di.DaggerAndroidDiComponent;
import net.dankito.appdownloader.util.android.IActivityResultListener;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MainActivity extends AppCompatActivity {


  protected AndroidDiComponent component;

  protected Map<Integer, IActivityResultListener> activityResultListeners = new ConcurrentHashMap<>();

  protected ViewPager viewPager;

  protected TabLayout tabLayout;

  protected ActivityMainTabsAdapter tabsAdapter;


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupDependencyInjection();

    setupUi();
  }

  protected void setupDependencyInjection() {
    component = DaggerAndroidDiComponent.builder()
                .androidDiContainer(new AndroidDiContainer(this))
                .build();

    component.inject(this);
  }

  protected void setupUi() {
    setContentView(R.layout.activity_main);

    Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    tabsAdapter = new ActivityMainTabsAdapter(this);

    viewPager = (ViewPager) findViewById(R.id.pager);
    viewPager.setAdapter(tabsAdapter);

    tabLayout = (TabLayout) findViewById(R.id.tabLayout);
    tabLayout.setupWithViewPager(viewPager);
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
