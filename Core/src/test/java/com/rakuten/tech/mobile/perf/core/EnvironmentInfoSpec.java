package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class EnvironmentInfoSpec {

  @Mock TelephonyManager telephonyManager;
  @Mock ActivityManager activityManager;
  @Mock Context context;
  private CachingObservable<LocationData> location = new CachingObservable<LocationData>(null);
  private CachingObservable<Float> batteryinfo = new CachingObservable<Float>(null);

  @Before public void initMocks() {
    MockitoAnnotations.initMocks(this);
    when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
    when(context.getSystemService(Activity.ACTIVITY_SERVICE)).thenReturn(activityManager);
  }

  @Test
  public void shouldValidateDeviceInfo() {
    EnvironmentInfo info = new EnvironmentInfo(context, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.device).isEqualTo(Build.MODEL);
    assertThat(info.osName).isEqualTo("android");
    assertThat(info.osVersion).isEqualTo(Build.VERSION.RELEASE);
  }

  @Test
  public void shouldPointToDefaultRegionWhenLocationIsNotUpdated() {
    Locale.setDefault(new Locale("testLanguage", "Test-Locale-Country", "testVariant"));
    EnvironmentInfo info = new EnvironmentInfo(context, location, batteryinfo);

    assertThat(info.getRegion()).isEqualTo(null);
    assertThat(info.getCountry()).isEqualTo("TEST-LOCALE-COUNTRY");
  }

  @Test
  public void shouldGetCountryAndRegionFromLocationUpdate() {
    LocationData locationData = new LocationData("IN", "Hyderabad");
    EnvironmentInfo info = new EnvironmentInfo(context, location, batteryinfo);

    location.publish(locationData);

    assertThat(info.getCountry()).isEqualTo(locationData.country);
    assertThat(info.getRegion()).isEqualTo(locationData.region);
  }

  @Test
  public void shouldUseCachedLocationForInstanceCreation() {
    LocationData cachedData = new LocationData("JP", "Tokyo");
    location.publish(cachedData);

    EnvironmentInfo info = new EnvironmentInfo(context, location, batteryinfo);

    assertThat(info.getCountry()).isEqualTo(cachedData.country);
    assertThat(info.getRegion()).isEqualTo(cachedData.region);
  }
}
