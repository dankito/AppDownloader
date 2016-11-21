package net.dankito.appdownloader;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.KeyEvent;
import android.widget.AdapterView;
import android.widget.EditText;

import net.dankito.appdownloader.utils.AdapterIdlingResource;
import net.dankito.appdownloader.utils.Matchers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressKey;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumentation test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class AppSearchResultsFragmentSmokeTest {

  protected AdapterIdlingResource adapterIdlingResource = null;


  @Rule
  public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(
      MainActivity.class);


  @Before
  public void setUp() {

  }


  @After
  public void unregisterIntentServiceIdlingResource() {
    if(adapterIdlingResource != null) {
      Espresso.unregisterIdlingResources(adapterIdlingResource);
    }
  }


  @Test
  public void searchForApps_CorrectAmountOfSearchResultsGetLoadedIntoListView() throws Exception {
    onView(withId(R.id.fabSearchApps)).perform(click());

    onView(withId(R.id.search)).perform(click());
    onView(isAssignableFrom(EditText.class)).perform(typeText("video"), pressKey(KeyEvent.KEYCODE_ENTER), closeSoftKeyboard());

    waitTillAdapterLoaded(R.id.lstvwAppSearchResults);
    onView(isAssignableFrom(EditText.class)).perform(closeSoftKeyboard()); // it's essential to call anything that performs an action on UI thread after registering and IdlingResource

    onView(withId(R.id.lstvwAppSearchResults)).check(matches(Matchers.withListSize(PlayStoreAppSearcher.MAX_SEARCH_RESULT_PAGE_NUMBER * PlayStoreAppSearcher.COUNT_SEARCH_RESULTS_PER_PAGE)));
  }

  protected void waitTillAdapterLoaded(int adapterViewResourceId) {
    AdapterView adapterView = (AdapterView)mActivityRule.getActivity().findViewById(adapterViewResourceId);

    adapterIdlingResource = new AdapterIdlingResource(adapterView.getAdapter(), PlayStoreAppSearcher.COUNT_SEARCH_RESULTS_PER_PAGE);
    Espresso.registerIdlingResources(adapterIdlingResource);
  }
}
