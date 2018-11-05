package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.runtime.MockedQueue;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import com.rakuten.tech.mobile.perf.runtime.shadow.RequestQueueShadow;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(
    shadows = {
      RequestQueueShadow.class, // prevent network requests from runtime side
    })
public class LocationStoreSpec extends RobolectricUnitSpec {

  @Rule public TestData location = new TestData("geolocation-api-response.json");

  @Mock PackageManager packageManager;
  private SharedPreferences prefs;
  /* Spy */ private Context context;
  /* Spy */ private MockedQueue queue;

  private LocationStore locationStore;

  @SuppressLint("ApplySharedPref")
  @Before
  public void init() throws PackageManager.NameNotFoundException {
    RequestQueueShadow.queue = spy(new MockedQueue());
    queue = RequestQueueShadow.queue;
    context = spy(RuntimeEnvironment.application);
    when(context.getPackageManager()).thenReturn(packageManager);
    prefs = context.getSharedPreferences("app_performance", Context.MODE_PRIVATE);
    prefs.edit().clear().apply();
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
  }

  @Test
  public void shouldRequestLocationOnEmptyCache() throws JSONException {
    queue.rule().whenClass(GeoLocationRequest.class).returnNetworkResponse(200, location.content);

    locationStore = new LocationStore(context, queue, "", "");

    queue.verify();
  }

  @Test
  public void shouldCacheLocationOnEmptyCache() throws JSONException {
    LocationData expectedValue = new LocationData("JP", "Tokyo");
    queue.rule().whenClass(GeoLocationRequest.class).returnNetworkResponse(200, location.content);

    locationStore = new LocationStore(context, queue, "", "");

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue.country).isEqualTo(expectedValue.country);
    assertThat(storeValue.region).isEqualTo(expectedValue.region);
  }

  @Test
  public void shouldUseCachedLocationForInstanceCreation() throws JSONException {
    LocationData prefsValue = new LocationData("JP", "Tokyo");
    prefs.edit().putString("location_key", new Gson().toJson(prefsValue)).apply();

    locationStore = new LocationStore(context, queue, "", "");

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue.country).isEqualTo(prefsValue.country);
    assertThat(storeValue.region).isEqualTo(prefsValue.region);
  }

  @Test
  public void shouldUseNullLocationOnEmptyCacheForInstanceCreation() throws JSONException {
    locationStore = new LocationStore(context, queue, "", "");

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue).isEqualTo(null);
  }

  @Test
  public void shouldNotFailOnFailedLocationRequest() {
    queue.rule().whenClass(GeoLocationRequest.class).returnError(new VolleyError(new Throwable()));

    locationStore = new LocationStore(context, queue, "", "");

    queue.verify();
  }

  @Test
  public void shouldDoWhatWhenSubscriptionKeyIsMissing() {
    locationStore = new LocationStore(context, queue, null, "");
  }
}
