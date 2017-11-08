package com.rakuten.tech.mobile.perf.core;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Locale;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EnvironmentInfoSpec {

  @Mock TelephonyManager tm;
  @Mock Context ctx;
  private CachingObservable<LocationData> location = new CachingObservable<LocationData>(null);
  private CachingObservable<Float> batteryinfo = new CachingObservable<Float>(null);
  private final String simCountry = "TEST-SIM-COUNTRY";
  private final String networkOperator = "test-network-operator";

  @Before public void initMocks() {
    MockitoAnnotations.initMocks(this);
    when(tm.getSimCountryIso()).thenReturn(simCountry);
    when(tm.getNetworkOperatorName()).thenReturn(networkOperator);
  }

  @Test
  public void shouldReadCountryAndNetworkFromTelephonyManager() {
    when(ctx.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(tm);
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualTo(simCountry);
    assertThat(info.network).isEqualTo(networkOperator);
  }

  @Test
  public void shouldValidateDeviceInfo() {
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.device).isEqualTo(Build.MODEL);
    assertThat(info.osName).isEqualTo("android");
    assertThat(info.osVersion).isEqualTo(Build.VERSION.RELEASE);
  }

  @Test
  public void shouldPointToDefaultCountryAndRegionWhenLocationIsNotUpdated() {
    Locale.setDefault(new Locale("testLanguage", "Test-Locale-Country", "testVariant"));
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);

    assertThat(info.getRegion()).isEqualTo(null);
    assertThat(info.getCountry()).isEqualTo("TEST-LOCALE-COUNTRY");
  }

  @Test
  public void shouldGetCountryAndRegionFromLocationUpdate() {
    LocationData locationData = new LocationData("IN", "Hyderabad");
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);

    location.publish(locationData);

    assertThat(info.getCountry()).isEqualTo(locationData.country);
    assertThat(info.getRegion()).isEqualTo(locationData.region);
  }

  @Test
  public void shouldUseCachedLocationForInstanceCreation() {
    LocationData cachedData = new LocationData("JP", "Tokyo");
    location.publish(cachedData);

    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);

    assertThat(info.getCountry()).isEqualTo(cachedData.country);
    assertThat(info.getRegion()).isEqualTo(cachedData.region);
  }

  @Test
  public void shouldFallbackToReadCountryFromLocaleIfTelephonyManagerIsNull() {
    when(ctx.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualToIgnoringCase(Locale.getDefault().getCountry());
  }

  @SuppressWarnings("RedundantStringConstructorCall")
  @Test
  public void shouldFallbackToReadCountryFromLocaleIfCountryIsEmpty() {
    when(ctx.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(tm);
    // prevent string literal from being "intern"ed so `== ""` is false in test setting, see
    // http://stackoverflow.com/questions/27473457/in-java-why-does-string-string-evaluate-to-true-inside-a-method-as-opposed
    when(tm.getSimCountryIso()).thenReturn(new String(""));
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualToIgnoringCase(Locale.getDefault().getCountry());
  }

  @Test
  public void shouldNormalizeCountryCodeToUppercase() {
    when(ctx.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);
    Locale.setDefault(new Locale("testLanguage", "Test-Locale-Country", "testVariant"));
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualTo("TEST-LOCALE-COUNTRY");

    when(ctx.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(tm);
    when(tm.getSimCountryIso()).thenReturn("TEST-SIM-COUNTRY");

    info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualTo("TEST-SIM-COUNTRY");
  }

}
