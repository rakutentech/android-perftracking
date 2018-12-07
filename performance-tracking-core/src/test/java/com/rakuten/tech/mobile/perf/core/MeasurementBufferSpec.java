package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class MeasurementBufferSpec {

  private MeasurementBuffer buffer;

  @Before public void init() {
    buffer = new MeasurementBuffer();
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
    fillBuffer(buffer, MeasurementBuffer.SIZE);

    assertThat(buffer.next()).isNull();
  }

  @Test public void shouldProvideSequentialIdAfterFullBufferHasCleared() {
    fillBuffer(buffer, MeasurementBuffer.SIZE);

    buffer.next();

    for (int i = 1; i <= MeasurementBuffer.SIZE; i++) {
      buffer.getByTrackingId(i).clear();
    }

    assertThat(buffer.next().trackingId).isEqualTo(513);
  }

  @Test public void shouldNotUseZeroId() {
    fillBuffer(buffer, MeasurementBuffer.SIZE);

    assertThat(buffer.getByTrackingId(0)).isNull();
  }

  @Test public void shouldUseNegativeIdWhenIntegerOverflowOccurs() {
    MeasurementBuffer negativeBuffer = new MeasurementBuffer(Integer.MAX_VALUE + 1);

    assertThat(negativeBuffer.next().trackingId).isEqualTo(Integer.MIN_VALUE);
  }

  @Test public void shouldGetMeasurementAtIndex() {
    Measurement next = buffer.next();

    fillBuffer(buffer, 10); // pretend some more tracking happens inbetween

    assertThat(next).isEqualTo(buffer.at(1));
  }

  @Test public void shouldLookupMeasurementById() {
    Measurement next = buffer.next();

    fillBuffer(buffer, 10); // pretend some more tracking happens inbetween

    Measurement other = buffer.getByTrackingId(next.trackingId);
    assertThat(next).isEqualTo(other);
  }

  @Test public void shouldReturnNullForInvalidId() {
    Measurement next = buffer.next();
    Measurement other = buffer.getByTrackingId(next.trackingId + 1);
    assertThat(other).isNull();
  }

  @Test public void shouldReturnCount() {
    buffer.next();
    buffer.next();

    assertThat(buffer.count(1)).isEqualTo(2);
  }

  @Test public void shouldReturnCountEqualToBufferSizeWhenFull() {
    fillBuffer(buffer, MeasurementBuffer.SIZE);

    assertThat(buffer.count(1)).isEqualTo(MeasurementBuffer.SIZE);
  }

  @Test public void shouldReturnCountWhenMeasurementsHaveBeenCleared() {
    for (int i = 1; i <= MeasurementBuffer.SIZE; i++) {
      buffer.next().clear();
    }

    fillBuffer(buffer, 10);

    assertThat(buffer.count(MeasurementBuffer.SIZE + 1)).isEqualTo(10);
  }

  private static void fillBuffer(MeasurementBuffer buffer, int count) {
    for (int i = 1; i <= count; i++) {
      buffer.next();
    }
  }
}
