package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Test;

public class MeasurementSpec {

  @Test public void shouldResetAllContentOnClear() {
    Measurement measurement = new Measurement();
    measurement.a = "a";
    measurement.b = "b";
    measurement.c = "c";
    measurement.startTime = 12345L;
    measurement.endTime = 54321L;
    measurement.trackingId = 9; // the best number
    measurement.type = Measurement.METRIC;
    measurement.activityName = "TestActivity";

    measurement.clear();
    assertThat(measurement.a).isNull();
    assertThat(measurement.b).isNull();
    assertThat(measurement.c).isNull();
    assertThat(measurement.activityName).isNull();
    assertThat(measurement.type).isEqualTo((byte) 0);
    assertThat(measurement.trackingId).isEqualTo(0);
    assertThat(measurement.startTime).isEqualTo(0L);
    assertThat(measurement.endTime).isEqualTo(0L);
  }

  @Test public void shouldNotFailOnDoubleClear() {
    Measurement measurement = new Measurement();
    measurement.a = "a";
    measurement.b = "b";
    measurement.c = "c";
    measurement.startTime = 12345L;
    measurement.endTime = 54321L;
    measurement.trackingId = 9; // the best number
    measurement.type = Measurement.METRIC;
    measurement.activityName = "TestActivity";

    measurement.clear();
    measurement.clear();
    assertThat(measurement.a).isNull();
    assertThat(measurement.b).isNull();
    assertThat(measurement.c).isNull();
    assertThat(measurement.activityName).isNull();
    assertThat(measurement.type).isEqualTo((byte) 0);
    assertThat(measurement.trackingId).isEqualTo(0);
    assertThat(measurement.startTime).isEqualTo(0L);
    assertThat(measurement.endTime).isEqualTo(0L);
  }
}
