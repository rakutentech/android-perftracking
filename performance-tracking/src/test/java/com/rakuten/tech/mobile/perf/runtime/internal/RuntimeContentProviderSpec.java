package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import com.rakuten.tech.mobile.perf.runtime.shadow.NetworkSecurityPolicyShadow;
import com.rakuten.tech.mobile.perf.runtime.shadow.TrackerShadow;
import com.rakuten.tech.mobile.perf.runtime.shadow.UtilShadow;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@Config(
    shadows = {
        NetworkSecurityPolicyShadow.class,
        TrackerShadow.class,
        StoreShadow.class, // fake cache
        UtilShadow.class // fake debug build
    })
public class RuntimeContentProviderSpec extends RobolectricUnitSpec {

  @Rule
  public TestData config = new TestData("configuration-api-response.json");
  @Rule
  public TestData configZeroPercent = new TestData("configuration-api-response-zero-percent.json");
  @Rule
  public MockWebServer server = new MockWebServer();

  @Mock
  private PackageManager packageManager;

  private RuntimeContentProvider provider;

  @Before
  public void init() throws PackageManager.NameNotFoundException {
    provider = spy(new RuntimeContentProvider());
    Context context = spy(RuntimeEnvironment.application);
    when(provider.getContext()).thenReturn(context);
    when(context.getPackageManager()).thenReturn(packageManager);
    PackageInfo pkgInfo = new PackageInfo();
    pkgInfo.versionName = "testVersion";
    when(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(pkgInfo);
    ApplicationInfo appInfo = new ApplicationInfo();
    appInfo.metaData = new Bundle();
    appInfo.metaData.putCharSequence("com.rakuten.tech.mobile.relay.AppId", "testAppId");
    appInfo.metaData.putCharSequence("com.rakuten.tech.mobile.perf.ConfigurationUrlPrefix", "https://config.example.com/");
    appInfo.metaData.putCharSequence("com.rakuten.tech.mobile.perf.LocationUrlPrefix", "https://location.example.com/");
    when(packageManager.getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA))
        .thenReturn(appInfo);
    TrackingManager.INSTANCE = null;
    clearInvocations(TrackerShadow.mockTracker);
    UtilShadow.mockDebugBuild = false;
  }

  @Test
  public void shouldNotStartTrackingOnEmptyCache() {
    StoreShadow.cachedContent = null;

    provider.onCreate();

    assertThat(TrackingManager.INSTANCE).isNull();
    verify(TrackerShadow.mockTracker, never()).startMetric(anyString());
  }

  @Test
  public void shouldStartTrackingAndLaunchMetricOnCachedConfig() {
    StoreShadow.cachedContent = new ConfigurationResponse(config.content);

    provider.onCreate();

    assertThat(TrackingManager.INSTANCE).isNotNull();
    verify(TrackerShadow.mockTracker).startMetric("_launch");
  }

  @Test
  public void shouldStartTrackingForDebugBuildEvenifEnablePercentIsZero() {
    StoreShadow.cachedContent = new ConfigurationResponse(configZeroPercent.content);
    UtilShadow.mockDebugBuild = true;

    provider.onCreate();

    assertThat(TrackingManager.INSTANCE).isNotNull();
    verify(TrackerShadow.mockTracker).startMetric("_launch");
  }

  @Test
  public void shouldNotFailOnMissingPackageInfo() throws PackageManager.NameNotFoundException {
    doThrow(new PackageManager.NameNotFoundException())
        .when(packageManager)
        .getPackageInfo(anyString(), anyInt());

    provider.onCreate();

    // no exception
  }

  @Test
  public void shouldStartTrackingEvenWhenPackageAndAppInfoIsMissing()
      throws PackageManager.NameNotFoundException {
    StoreShadow.cachedContent = new ConfigurationResponse(config.content);
    doThrow(new PackageManager.NameNotFoundException())
        .when(packageManager)
        .getPackageInfo(anyString(), anyInt());
    doThrow(new PackageManager.NameNotFoundException())
        .when(packageManager)
        .getApplicationInfo(anyString(), anyInt());

    provider.onCreate();

    assertThat(TrackingManager.INSTANCE).isNotNull();
    verify(TrackerShadow.mockTracker).startMetric("_launch");
  }

  @Test
  public void shouldNotImplementAnyContentProviderMethods() {
    assertThat(provider.query(null, null, null, null, null)).isNull();
    assertThat(provider.getType(null)).isNull();
    assertThat(provider.insert(null, null)).isNull();
    assertThat(provider.delete(null, null, null)).isEqualTo(0);
    assertThat(provider.update(null, null, null, null)).isEqualTo(0);
  }
}
