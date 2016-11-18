package net.dankito.appdownloader.di;

import net.dankito.appdownloader.dialogs.AppDetailsDialog;
import net.dankito.appdownloader.fragments.AppSearchResultsFragment;
import net.dankito.appdownloader.MainActivity;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by ganymed on 03/11/16.
 */
@Singleton
@Component(modules = { AndroidDiContainer.class } )
public interface AndroidDiComponent {

  // to update the fields in your activities
  void inject(MainActivity activity);

  void inject(AppSearchResultsFragment appSearchResultsFragment);

  void inject(AppDetailsDialog appDetailsDialog);

}
