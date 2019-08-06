package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import com.rakuten.tech.mobile.perf.runtime.shadow.NetworkSecurityPolicyShadow;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.skyscreamer.jsonassert.JSONAssert;
import retrofit2.Retrofit;

@Config(shadows = {NetworkSecurityPolicyShadow.class})
public class ConfigStoreSpec extends RobolectricUnitSpec {

  @Rule
  public TestData config = new TestData("configuration-api-response.json");
  @Rule
  public MockWebServer server = new MockWebServer();

  @Mock
  private PackageManager packageManager;
  private ConfigApi api;
  private SharedPreferences prefs;
  /* Spy */ private Context context;

  private ConfigStore configStore;
  private String appId = "testAppId";

  @SuppressLint("ApplySharedPref")
  @Before
  public void init() throws PackageManager.NameNotFoundException {
    context = spy(RuntimeEnvironment.application);
    when(context.getPackageManager()).thenReturn(packageManager);
    prefs = context.getSharedPreferences("app_performance", Context.MODE_PRIVATE);
    prefs.edit().clear().apply();
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
    PackageInfo pkgInfo = new PackageInfo();
    pkgInfo.versionName = "testVersion";
    when(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(pkgInfo);

    api = new Retrofit.Builder()
        .baseUrl(server.url("/").toString())
        .addConverterFactory(new ConverterFactory())
        .callbackExecutor(Executors.newSingleThreadExecutor())
        .build()
        .create(ConfigApi.class);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailToCreateConfigStoreOnNullAppId() {
    configStore = new ConfigStore(context, null, "", api);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailToCreateConfigStoreOnEmptyAppId() {
    configStore = new ConfigStore(context, "", "", api);
  }

  @Test
  public void shouldDoWhatWhenSubscriptionKeyIsMissing() {
    // TODO: what is the expected behavior?
    configStore = new ConfigStore(context, appId, null, api);
  }

  @Test
  public void shouldAddRasPrefixToSubscriptionKey() throws InterruptedException {
    configStore = new ConfigStore(context, appId, "test-subscription-key", api);

    assertThat(server.takeRequest().getHeader("apiKey"))
      .isEqualTo( "ras-test-subscription-key");
  }

  @Test
  public void shouldUseCachedConfigForInstanceCreation() throws JSONException {
    prefs.edit().putString("config_key", config.content).apply();

    // Cached config available
    configStore = new ConfigStore(context, appId, "", api);

    ConfigurationResponse cachedResponse = configStore.getObservable().getCachedValue();
    JSONAssert.assertEquals(config.content, cachedResponse.toString(), true);
  }

  @Test
  public void shouldUseNullConfigOnEmptyCacheOnInstanceCreation() {
    // EmptyCache
    configStore = new ConfigStore(context, appId, "", api);

    ConfigurationResponse storeValue = configStore.getObservable().getCachedValue();
    assertThat(storeValue).isEqualTo(null);
  }

  @Test
  public void shouldCreateConfigEvenWhenPackageIsMissing()
      throws PackageManager.NameNotFoundException, JSONException {
    prefs.edit().putString("config_key", config.content).apply();
    doThrow(new PackageManager.NameNotFoundException())
        .when(packageManager)
        .getPackageInfo(anyString(), anyInt());

    configStore = new ConfigStore(context, appId, null, api);

    ConfigurationResponse cachedResponse = configStore.getObservable().getCachedValue();
    JSONAssert.assertEquals(config.content, cachedResponse.toString(), true);
  }

  @Test
  public void shouldRequestConfigOnEmptyCache() {
    configStore = new ConfigStore(context, appId, "", api);

    await().atMost(1, TimeUnit.SECONDS).untilAsserted(
        () -> assertThat(server.getRequestCount()).isEqualTo(1)
    );
  }

  @Test
  public void shouldCacheConfigOnEmptyCache() throws JSONException {
    server.enqueue(new MockResponse().setBody(config.content));

    configStore = new ConfigStore(context, appId, "", api);

    await().atMost(1, TimeUnit.SECONDS).until(
        () -> configStore.getObservable().getCachedValue() != null
    );

    ConfigurationResponse cachedResponse = configStore.getObservable().getCachedValue();
    JSONAssert.assertEquals(config.content, cachedResponse.toString(), true);
  }

  @Test
  public void shouldNotFailOnFailedConfigRequest() {
    server.enqueue(new MockResponse().setResponseCode(500));

    configStore = new ConfigStore(context, appId, "", api);

    await().atMost(1, TimeUnit.SECONDS).catchUncaughtExceptions().until(
        () -> server.getRequestCount() == 1
    );

    // no exception
  }
}
