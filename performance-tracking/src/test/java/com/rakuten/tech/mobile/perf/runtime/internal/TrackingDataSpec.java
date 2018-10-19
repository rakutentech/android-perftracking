package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.support.annotation.NonNull;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import org.junit.Test;

public class TrackingDataSpec extends RobolectricUnitSpec {

  @Test
  public void shouldOnlyEqualForEqualIdAndEqualObject() {
    TrackingData original = data("id", "obj");

    assertThat(original).isEqualTo(original);
    assertThat(original).isEqualTo(data("id", "obj"));
    assertThat(original).isNotEqualTo(data("id", "OBJ"));
    assertThat(original).isNotEqualTo(data("ID", "obj"));
    assertThat(original).isNotEqualTo(data("id", null));
    assertThat(original).isNotEqualTo(new Object());
    assertThat(data("id", null)).isEqualTo(data("id", null));
  }

  @Test
  public void shouldCompareIdAndThenObject() {
    assertThat(data("1", 1)).isEqualByComparingTo(data("1", 1));
    assertThat(data("1", 1)).isLessThan(data("1", 2));
    assertThat(data("1", 1)).isLessThan(data("2", 1));
    assertThat(data("1", 1)).isGreaterThan(data("1", null));
    assertThat(data("1", null)).isEqualByComparingTo(data("1", null));
    assertThat(data("1", null)).isLessThan(data("1", 0));
    assertThat(data("1", 0)).isGreaterThan(data("1", null));
    assertThat(data("1", 1).compareTo(null)).isLessThan(0);
  }

  @NonNull
  private TrackingData data(String id, Comparable obj) {
    return new TrackingData(id, obj);
  }
}
