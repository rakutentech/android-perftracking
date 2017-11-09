package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.when;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class EnvironmentInfoSpec {

  @Mock TelephonyManager tm;
  @Mock ActivityManager am;
  @Mock Context ctx;
  private CachingObservable<LocationData> location = new CachingObservable<LocationData>(null);
  private CachingObservable<Float> batteryinfo = new CachingObservable<Float>(null);
  private final String simCountry = "TEST-SIM-COUNTRY";
  private final String networkOperator = "test-network-operator";
  private int invocationCount = 0;

  @Before public void initMocks() {
    MockitoAnnotations.initMocks(this);
    when(tm.getSimCountryIso()).thenReturn(simCountry);
    when(tm.getNetworkOperatorName()).thenReturn(networkOperator);
    /*
     * Because of the tricky setup of the SDK Context#getSystemService(String) will
     * always be called with null. So we rely on the knowledge of internal implementation, i.e.
     * the invocation order, to serve the correct stub. This is really shitty and for testing it
     * would be better to directly inject the 2 system services into the constructor.
     * In short: this a hacky workaround to make the stubbing work. This is likely to break when
     * we refactor or evolve EnvironmentInfo
     *
     * Setting the tm or am to null (or any other reference) will make the context return null -
     * be careful!
     */
    invocationCount = 0;
    when(ctx.getSystemService(nullable(String.class)))
        .thenAnswer(new Answer<Object>() {
          @Override public Object answer(InvocationOnMock invocation) throws Throwable {
            return invocationCount++ % 2 == 0 ? am : tm;
          }
        });
  }


  @Test
  public void shouldReadCountryAndNetworkFromTelephonyManager() {
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
    tm = null;
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
    tm = null;
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualToIgnoringCase(Locale.getDefault().getCountry());
  }

  @SuppressWarnings("RedundantStringConstructorCall")
  @Test
  public void shouldFallbackToReadCountryFromLocaleIfCountryIsEmpty() {
    // prevent string literal from being "intern"ed so `== ""` is false in test setting, see
    // http://stackoverflow.com/questions/27473457/in-java-why-does-string-string-evaluate-to-true-inside-a-method-as-opposed
    when(tm.getSimCountryIso()).thenReturn(new String(""));
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualToIgnoringCase(Locale.getDefault().getCountry());
  }

  @Test
  public void shouldNormalizeCountryCodeToUppercase() {
    TelephonyManager backupTm = tm;
    tm = null;
    Locale.setDefault(new Locale("testLanguage", "Test-Locale-Country", "testVariant"));
    EnvironmentInfo info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualTo("TEST-LOCALE-COUNTRY");

    tm = backupTm;
    when(tm.getSimCountryIso()).thenReturn("TEST-SIM-COUNTRY");

    info = new EnvironmentInfo(ctx, location, batteryinfo);
    assertThat(info).isNotNull();
    assertThat(info.getCountry()).isEqualTo("TEST-SIM-COUNTRY");
  }

}
