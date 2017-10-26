package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.os.Parcel;
import com.google.gson.Gson;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import com.rakuten.tech.mobile.perf.runtime.shadow.ParcelShadow;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.annotation.Config;

@Config(shadows = {ParcelShadow.class})
public class ConfigurationResultSpec extends RobolectricUnitSpec {

  @Rule public TestData data = new TestData("configuration-api-response.json");

  @Test public void shouldParcelWithoutError() {
    ConfigurationResult response = new Gson().fromJson(data.content, ConfigurationResult.class);
    Parcel parcel = Parcel.obtain();

    response.writeToParcel(parcel, 0);
    ConfigurationResult fromParcel = ConfigurationResult.CREATOR.createFromParcel(parcel);

    assertThat(response.getHeader()).containsAllEntriesOf(fromParcel.getHeader());
    assertThat(fromParcel.getHeader()).containsAllEntriesOf(response.getHeader());
    assertThat(response.getEnablePercent()).isEqualTo(fromParcel.getEnablePercent());
    assertThat(response.getSendUrl()).isEqualTo(fromParcel.getSendUrl());
    assertThat(response.describeContents()).isEqualTo(fromParcel.describeContents());
  }

  @Test public void shouldImplementParcelableMiscMethods() {
    ConfigurationResult[] array = ConfigurationResult.CREATOR.newArray(0);
    assertThat(array).isNotNull();
  }
}
