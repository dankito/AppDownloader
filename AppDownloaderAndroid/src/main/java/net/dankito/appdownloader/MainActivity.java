package net.dankito.appdownloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import net.dankito.appdownloader.di.AndroidDiComponent;
import net.dankito.appdownloader.di.DaggerAndroidDiComponent;

public class MainActivity extends AppCompatActivity {


  protected AndroidDiComponent component;


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
        .build();

    component.inject(this);
  }


  public AndroidDiComponent getComponent() {
    return component;
  }

}
