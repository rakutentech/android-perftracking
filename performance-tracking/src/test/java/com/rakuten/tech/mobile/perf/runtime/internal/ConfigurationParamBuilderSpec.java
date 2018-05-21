package com.rakuten.tech.mobile.perf.runtime.internal;

import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import org.junit.Before;
import org.junit.Test;


public class ConfigurationParamBuilderSpec extends RobolectricUnitSpec {

  private ConfigurationParam.Builder builder;

  @Before public void initValidBuilder() {
    builder = new ConfigurationParam.Builder()
        .setAppId("testAppId")
        .setAppVersion("testAppVersion")
        .setCountryCode("testCountryCode")
        .setPlatform("testPlatform")
        .setSdkVersion("testSdkVersion");
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceNonNullPlatform() {
    builder.setPlatform(null);
    builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceAppId() {
    builder.setAppId(null);
    builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceCountryCode() {
    builder.setCountryCode(null);
    builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceAppVersion() {
    builder.setAppVersion(null);
    builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceSdkVersion() {
    builder.setSdkVersion(null);
    builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceOSVersion() {
    builder.setOsVersion(null);
    builder.build();
  }

  @Test(expected = IllegalStateException.class)
  public void shouldEnforceDevice() {
    builder.setDevice(null);
    builder.build();
  }
}
