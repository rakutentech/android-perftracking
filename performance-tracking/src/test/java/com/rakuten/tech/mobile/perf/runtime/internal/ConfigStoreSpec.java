package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
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
import org.skyscreamer.jsonassert.JSONAssert;

@Config(shadows = {
    RequestQueueShadow.class, // prevent network requests from runtime side
})
public class ConfigStoreSpec extends RobolectricUnitSpec {

  @Rule public TestData config = new TestData("configuration-api-response.json");

  @Mock PackageManager packageManager;
  /* Spy */ private SharedPreferences prefs;
  /* Spy */ private Context context;
  /* Spy */ private MockedQueue queue;

  private ConfigStore configStore;
  private String appId = "testAppId";

  @SuppressLint("ApplySharedPref")
  @Before public void init() throws PackageManager.NameNotFoundException {
    RequestQueueShadow.queue = spy(new MockedQueue());
    queue = RequestQueueShadow.queue;
    context = spy(RuntimeEnvironment.application);
    when(context.getPackageManager()).thenReturn(packageManager);
    prefs = spy(context.getSharedPreferences("app_performance", Context.MODE_PRIVATE));
    prefs.edit().clear().apply();
    clearInvocations(prefs);
    when(context.getSharedPreferences(anyString(), anyInt())).thenReturn(prefs);
    PackageInfo pkgInfo = new PackageInfo();
    pkgInfo.versionName = "testVersion";
    when(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(pkgInfo);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailToCreateConfigStoreOnNullAppId() {
    configStore = new ConfigStore(context, queue, null, "", null);
  }

  @Test(expected = IllegalStateException.class)
  public void shouldFailToCreateConfigStoreOnEmptyAppId() {
    configStore = new ConfigStore(context, queue, "", "", null);
  }

  @Test public void shouldRequestConfigOnEmptyCache() throws JSONException {
    queue.rule().whenClass(ConfigurationRequest.class).returnNetworkResponse(200, config.content);

    configStore = new ConfigStore(context, queue, appId, "", null);

    queue.verify();
  }

  @Test public void shouldCacheConfigOnEmptyCache() throws JSONException {
    queue.rule().whenClass(ConfigurationRequest.class).returnNetworkResponse(200, config.content);

    configStore = new ConfigStore(context, queue, appId, "", null);

    ConfigurationResult cachedResponse = configStore.getObservable().getCachedValue();
    JSONAssert.assertEquals(config.content, new Gson().toJson(cachedResponse), true);
  }

  @Test public void shouldUseCachedConfigForInstanceCreation() throws JSONException {
    prefs.edit().putString("config_key", config.content).apply();

    // Cached config available
    configStore = new ConfigStore(context, queue, appId, "", null);

    ConfigurationResult cachedResponse = configStore.getObservable().getCachedValue();
    JSONAssert.assertEquals(config.content, new Gson().toJson(cachedResponse), true);
  }

  @Test public void shouldUseNullConfigOnEmptyCacheOnInstanceCreation() throws JSONException {
    // EmptyCache
    configStore = new ConfigStore(context, queue, appId, "", null);

    ConfigurationResult storeValue = configStore.getObservable().getCachedValue();
    assertThat(storeValue).isEqualTo(null);
  }

  @Test public void shouldNotFailOnFailedConfigRequest() {
    queue.rule().whenClass(ConfigurationRequest.class)
        .returnError(new VolleyError(new Throwable()));

    configStore = new ConfigStore(context, queue, appId, "", null);

    queue.verify();
  }

  @Test public void shouldDoWhatWhenSubscriptionKeyIsMissing() {
    configStore = new ConfigStore(context, queue, appId, null, null);
  }

  @Test public void shouldCreateConfigEvenWhenPackageIsMissing()
      throws PackageManager.NameNotFoundException, JSONException {
    prefs.edit().putString("config_key", config.content).apply();
    doThrow(new PackageManager.NameNotFoundException())
        .when(packageManager).getPackageInfo(anyString(), anyInt());

    configStore = new ConfigStore(context, queue, appId, null, null);

    ConfigurationResult cachedResponse = configStore.getObservable().getCachedValue();
    JSONAssert.assertEquals(config.content, new Gson().toJson(cachedResponse), true);
  }

}
