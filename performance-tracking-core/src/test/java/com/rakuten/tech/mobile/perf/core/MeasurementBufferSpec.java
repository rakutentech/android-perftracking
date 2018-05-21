package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MeasurementBufferSpec {

  private MeasurementBuffer buffer;

  @Before public void init() {
    buffer = new MeasurementBuffer();
  }

  @Test public void shouldInitializePublicFields() {
    assertThat(buffer.at).isNotNull();
    assertThat(buffer.nextTrackingId).isNotNull();
    assertThat(buffer.at).isNotEmpty();
    for (Measurement m : buffer.at) {
      assertThat(m).isNotNull();
    }
  }

  @Test public void shouldProvideBlankMeasurementWhenBufferHasSpace() {
    Measurement next = buffer.next();
    assertThat(next.a).isNull();
    assertThat(next.b).isNull();
    assertThat(next.c).isNull();
    assertThat(next.startTime).isEqualTo(0L);
    assertThat(next.endTime).isEqualTo(0L);
    assertThat(next.type).isEqualTo((byte) 0);
    assertThat(next.trackingId).isEqualTo(1); // 1 for the first
  }

  @Test public void shouldProvideNullWhenBufferIsFull() {
    for (int i = 0; i < buffer.at.length; i++) {
      buffer.next();
    }
    assertThat(buffer.next()).isNull();
  }

  @Test public void shouldNotFailOnNegativeIndex() {
    buffer.nextTrackingId.set(-5);
    Measurement next = buffer.next();
    assertThat(next.trackingId).isEqualTo(-5);
  }

  @Test public void shouldNotFailOnZeroIndex() {
    buffer.nextTrackingId.set(0);
    Measurement next = buffer.next();
    assertThat(next.trackingId).isEqualTo(1);
  }

  @Test public void shouldLookupMeasurementById() {
    Measurement next = buffer.next();

    for (int i = 0; i < 10; i++) { // pretend some more tracking happens inbetween
      buffer.next();
    }

    Measurement other = buffer.getByTrackingId(next.trackingId);
    assertThat(next).isEqualTo(other);
  }

  @Test public void shouldLookupNullForInvalidId() {
    Measurement next = buffer.next();
    Measurement other = buffer.getByTrackingId(next.trackingId + 1);
    assertThat(other).isNull();
  }
}
