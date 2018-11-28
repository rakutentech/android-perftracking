package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import com.rakuten.tech.mobile.perf.runtime.shadow.NetworkSecurityPolicyShadow;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import retrofit2.Retrofit;

@Config(shadows = {NetworkSecurityPolicyShadow.class})
public class LocationStoreSpec extends RobolectricUnitSpec {

  @Rule
  public TestData location = new TestData("geolocation-api-response.json");
  @Rule
  public MockWebServer server = new MockWebServer();

  @Mock
  private PackageManager packageManager;
  private SharedPreferences prefs;
  /* Spy */ private Context context;
  private GeoLocationApi api;

  private LocationStore locationStore;

  @SuppressLint("ApplySharedPref")
  @Before
  public void init() {
    context = spy(RuntimeEnvironment.application);
    when(context.getPackageManager()).thenReturn(packageManager);
    prefs = context.getSharedPreferences("app_performance", Context.MODE_PRIVATE);
    prefs.edit().clear().apply();
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);

    api = new Retrofit.Builder()
        .baseUrl(server.url("/").toString())
        .addConverterFactory(new ConverterFactory())
        .callbackExecutor(Executors.newSingleThreadExecutor())
        .build()
        .create(GeoLocationApi.class);
  }

  @Test
  public void shouldRequestLocationOnEmptyCache() {
    server.enqueue(new MockResponse().setBody(location.content));

    locationStore = new LocationStore(context, "", api);

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(
        () -> assertThat(server.getRequestCount()).isEqualTo(1)
    );
  }

  @Test
  public void shouldCacheLocationOnEmptyCache() {
    server.enqueue(new MockResponse().setBody(location.content));

    locationStore = new LocationStore(context, "", api);
    await().atMost(1, TimeUnit.SECONDS).until(
        () -> locationStore.getObservable().getCachedValue() != null
    );

    LocationData expectedValue = new LocationData("JP", "Tokyo");
    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue.country).isEqualTo(expectedValue.country);
    assertThat(storeValue.region).isEqualTo(expectedValue.region);
  }

  @Test
  public void shouldUseCachedLocationForInstanceCreation() {
    LocationData prefsValue = new LocationData("JP", "Tokyo");
    prefs.edit().putString("location_key", LocationStore.toJsonString(prefsValue)).apply();

    locationStore = new LocationStore(context, "", api);

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue.country).isEqualTo(prefsValue.country);
    assertThat(storeValue.region).isEqualTo(prefsValue.region);
  }

  @Test
  public void shouldUseNullLocationOnEmptyCacheForInstanceCreation() {
    locationStore = new LocationStore(context, "", api);

    LocationData storeValue = locationStore.getObservable().getCachedValue();
    assertThat(storeValue).isEqualTo(null);
  }

  @Test
  public void shouldNotFailOnFailedLocationRequest() {
    server.enqueue(new MockResponse().setResponseCode(500));

    locationStore = new LocationStore(context, "", api);

    await().atMost(1, TimeUnit.SECONDS).catchUncaughtExceptions().until(
        () -> server.getRequestCount() == 1
    );

    // no exception
  }

  @Test
  public void shouldDoWhatWhenSubscriptionKeyIsMissing() {
    locationStore = new LocationStore(context, null, api);
  }
}
