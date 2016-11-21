package net.dankito.appdownloader.utils;

import android.database.DataSetObserver;
import android.support.test.espresso.IdlingResource;
import android.widget.Adapter;

/**
 * Created by ganymed on 21/11/16.
 */

public class AdapterIdlingResource implements IdlingResource {

  protected Adapter adapter;

  protected int countUpdatesToWait;

  private ResourceCallback callback;


  public AdapterIdlingResource(Adapter adapter) {
    this(adapter, 1);
  }

  public AdapterIdlingResource(Adapter adapter, int countUpdatesToWait) {
    this.adapter = adapter;
    this.countUpdatesToWait = countUpdatesToWait;

    adapter.registerDataSetObserver(adapterDataSetObserver);
  }


  @Override
  public String getName() {
    return getClass().getName();
  }

  @Override
  public boolean isIdleNow() {
    return countUpdatesToWait == 0;
  }

  @Override
  public void registerIdleTransitionCallback(ResourceCallback callback) {
    this.callback = callback;
  }


  protected void dataSetChangedOrInvalidated() {
    countUpdatesToWait--;

    if(countUpdatesToWait <= 0) {
      adapter.unregisterDataSetObserver(adapterDataSetObserver);

      if(callback != null) {
        callback.onTransitionToIdle();
      }
    }
  }


  protected DataSetObserver adapterDataSetObserver = new DataSetObserver() {
    @Override
    public void onChanged() {
      super.onChanged();

      dataSetChangedOrInvalidated();
    }

    @Override
    public void onInvalidated() {
      super.onInvalidated();

      dataSetChangedOrInvalidated();
    }
  };

}
