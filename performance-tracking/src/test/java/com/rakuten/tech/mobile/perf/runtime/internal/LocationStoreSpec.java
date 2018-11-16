package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.android.volley.VolleyError;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.runtime.MockedQueue;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import com.rakuten.tech.mobile.perf.runtime.shadow.RequestQueueShadow;
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
  public void init() {
    RequestQueueShadow.queue = spy(new MockedQueue());
    queue = RequestQueueShadow.queue;
    context = spy(RuntimeEnvironment.application);
    when(context.getPackageManager()).thenReturn(packageManager);
    prefs = context.getSharedPreferences("app_performance", Context.MODE_PRIVATE);
    prefs.edit().clear().apply();
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
  }

  @Test
  public void shouldRequestLocationOnEmptyCache() {
    queue.rule().whenClass(GeoLocationRequest.class).returnNetworkResponse(200, location.content);

    locationStore = new LocationStore(context, queue, "", "");

    queue.verify();
  }

  @Test
  public void shouldCacheLocationOnEmptyCache() {
    LocationData expectedValue = new LocationData("JP", "Tokyo");
    queue.rule().whenClass(GeoLocationRequest.class).returnNetworkResponse(200, location.content);

    locationStore = new LocationStore(context, queue, "", "");

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue.country).isEqualTo(expectedValue.country);
    assertThat(storeValue.region).isEqualTo(expectedValue.region);
  }

  @Test
  public void shouldUseCachedLocationForInstanceCreation() {
    LocationData prefsValue = new LocationData("JP", "Tokyo");
    prefs.edit().putString("location_key", LocationStore.toJsonString(prefsValue)).apply();

    locationStore = new LocationStore(context, queue, "", "");

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue.country).isEqualTo(prefsValue.country);
    assertThat(storeValue.region).isEqualTo(prefsValue.region);
  }

  @Test
  public void shouldUseNullLocationOnEmptyCacheForInstanceCreation() {
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
