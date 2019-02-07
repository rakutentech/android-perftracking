package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.os.Parcel;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import org.junit.Rule;
import org.junit.Test;

public class ConfigurationResultSpec extends RobolectricUnitSpec {

  @Rule public TestData data = new TestData("configuration-api-response.json");

  @Test
  public void shouldParcelWithoutError() {

    ConfigurationResult response = new ConfigurationResult(data.content);
    Parcel parcel = Parcel.obtain();

    response.writeToParcel(parcel, 0);
    parcel.setDataPosition(0);
    ConfigurationResult fromParcel = ConfigurationResult.CREATOR.createFromParcel(parcel);

    assertThat(response.getHeader()).containsAllEntriesOf(fromParcel.getHeader());
    assertThat(fromParcel.getHeader()).containsAllEntriesOf(response.getHeader());
    assertThat(response.getEnablePercent()).isEqualTo(fromParcel.getEnablePercent());
    assertThat(response.shouldEnableNonMetricMeasurement())
        .isEqualTo(fromParcel.shouldEnableNonMetricMeasurement());
    assertThat(response.getSendUrl()).isEqualTo(fromParcel.getSendUrl());
    assertThat(response.shouldSendToPerfTracking()).isEqualTo(fromParcel.shouldSendToPerfTracking());
    assertThat(response.shouldSendToAnalytics()).isEqualTo(fromParcel.shouldSendToAnalytics());
    assertThat(response.describeContents()).isEqualTo(fromParcel.describeContents());
  }

  @Test
  public void shouldImplementParcelableMiscMethods() {
    ConfigurationResult[] array = ConfigurationResult.CREATOR.newArray(0);
    assertThat(array).isNotNull();
  }
}
