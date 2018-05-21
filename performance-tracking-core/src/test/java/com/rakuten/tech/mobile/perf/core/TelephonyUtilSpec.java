package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.telephony.TelephonyManager;
import java.util.Locale;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TelephonyUtilSpec {

  @Mock TelephonyManager telephonyManager;
  @Mock Context context;

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(telephonyManager);
  }

  @Test
  public void shouldReadCountryFromTelephonyManager() {
    when(telephonyManager.getSimCountryIso()).thenReturn("TEST-SIM-COUNTRY");

    assertThat(TelephonyUtil.getCountryCode(context)).isEqualTo("TEST-SIM-COUNTRY");
  }

  @Test
  public void shouldFallbackToReadCountryFromLocaleIfSimCountryIsEmpty() {
    when(telephonyManager.getSimCountryIso()).thenReturn("");
    Locale.setDefault(new Locale("testLanguage", "TEST-LOCALE-COUNTRY", "testVariant"));

    assertThat(TelephonyUtil.getCountryCode(context)).isEqualTo("TEST-LOCALE-COUNTRY");
  }

  @Test
  public void shouldFallbackToReadCountryFromLocaleIfSimCountryIsNull() {
    when(telephonyManager.getSimCountryIso()).thenReturn(null);
    Locale.setDefault(new Locale("testLanguage", "TEST-LOCALE-COUNTRY", "testVariant"));

    assertThat(TelephonyUtil.getCountryCode(context)).isEqualTo("TEST-LOCALE-COUNTRY");
  }

  @Test
  public void shouldFallbackToReadCountryFromLocaleIfTelephonyManagerIsNull() {
    when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);
    Locale.setDefault(new Locale("testLanguage", "TEST-LOCALE-COUNTRY", "testVariant"));

    assertThat(TelephonyUtil.getCountryCode(context)).isEqualTo("TEST-LOCALE-COUNTRY");
  }

  @Test
  public void shouldNormalizeCountryCodeToUppercase() {
    when(telephonyManager.getSimCountryIso()).thenReturn("");
    Locale.setDefault(new Locale("testLanguage", "Test-Locale-Country", "testVariant"));

    assertThat(TelephonyUtil.getCountryCode(context)).isEqualTo("TEST-LOCALE-COUNTRY");

    when(telephonyManager.getSimCountryIso()).thenReturn("test-sim-country");

    assertThat(TelephonyUtil.getCountryCode(context)).isEqualTo("TEST-SIM-COUNTRY");
  }

  @Test
  public void shouldReadNetworkFromTelephonyManager() {
    when(telephonyManager.getNetworkOperatorName()).thenReturn("test-network-operator");

    assertThat(TelephonyUtil.getNetwork(context)).isEqualTo("test-network-operator");
  }

  @Test
  public void shouldFallbackToDefaultWhenTelphonyMangerIsNull() {
    when(context.getSystemService(Context.TELEPHONY_SERVICE)).thenReturn(null);

    assertThat(TelephonyUtil.getNetwork(context)).isEqualTo("wifi");
  }

  @Test
  public void shouldFallbackToDefaultWhenNetworkNameIsEmpty() {
    when(telephonyManager.getNetworkOperatorName()).thenReturn("");

    assertThat(TelephonyUtil.getNetwork(context)).isEqualTo("wifi");
  }

  @Test
  public void shouldFallbackToDefaultWhenNetworkNameIsNull() {
    when(telephonyManager.getNetworkOperatorName()).thenReturn(null);

    assertThat(TelephonyUtil.getNetwork(context)).isEqualTo("wifi");
  }
}
