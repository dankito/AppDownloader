package net.dankito.appdownloader.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import net.dankito.appdownloader.MainActivity;
import net.dankito.appdownloader.R;
import net.dankito.appdownloader.di.AndroidDiComponent;

/**
 * Created by ganymed on 06/11/16.
 */

public abstract class FullscreenDialog extends Dialog {

  protected AppCompatActivity activity;

  protected Toolbar toolbar;


  public FullscreenDialog(AppCompatActivity context) {
    super(context, R.style.FullscreenDialog);

    this.activity = context;
  }


  protected void injectComponents(AndroidDiComponent component) {
    // may be overwritten in sub class
  }

  protected abstract int getLayoutId();

  protected abstract void setupUi(View rootView);


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    injectComponents(((MainActivity)activity).getComponent());

    requestWindowFeature(Window.FEATURE_ACTION_BAR);
    requestWindowFeature(Window.FEATURE_CONTEXT_MENU);

    LayoutInflater layoutInflater = activity.getLayoutInflater();
    View rootView = layoutInflater.inflate(getLayoutId(), null);
    addContentView(rootView, (new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)));

    setupToolbar(rootView);

    setupUi(rootView);
  }

  protected void setupToolbar(View rootView) {
    toolbar = (Toolbar)rootView.findViewById(R.id.toolbar);
    toolbar.setTitle("");

    activity.setSupportActionBar(toolbar);

    ActionBar actionBar = activity.getSupportActionBar();
    if(actionBar != null) {
      actionBar.setDisplayHomeAsUpEnabled(true);
      actionBar.setHomeButtonEnabled(true);
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();

    if (id == android.R.id.home) {
      hide();
      return true;
    }

    return super.onOptionsItemSelected(item);
  }

}
