package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNotNull;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import org.assertj.core.api.Condition;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SenderSpec {

  Sender sender;
  MeasurementBuffer measurementBuffer;
  Current current;
  @Mock EventWriter eventWriter;

  @Before public void init() {
    MockitoAnnotations.initMocks(this);
    measurementBuffer = new MeasurementBuffer();
    current = new Current();
    sender = new Sender(measurementBuffer, current, eventWriter, new Debug(), true);
  }

  public static class FullSendingBehaviorSpec extends SenderSpec {
    @Before public void initSender() {
      sender = new Sender(measurementBuffer, current, eventWriter, new Debug(), true);
    }

    @Test public void shouldSendMeasurements() throws IOException {
      setUpCustomMeasurements(measurementBuffer, 10);
      sender.send(0);
      ArgumentCaptor<Measurement> captor = ArgumentCaptor.forClass(Measurement.class);
      verify(eventWriter, times(1)).begin();
      verify(eventWriter, times(10)).write(captor.capture(), (String) isNull());
      for (Measurement measurement : captor.getAllValues()) {
        assertThat(measurement.a).isNull();// as measurements are cleared once sent.
      }
      verify(eventWriter, times(1)).end();
    }

    @Test public void shouldSendMetric() throws IOException {
      setUpCustomMetrics(measurementBuffer, 10);
      sender.send(0);
      ArgumentCaptor<Metric> captor = ArgumentCaptor.forClass(Metric.class);
      verify(eventWriter, times(1)).begin();
      verify(eventWriter, times(9)).write(captor.capture());
      for (Metric metric : captor.getAllValues()) {
        assertThat(metric.id).isEqualTo("custom-metric");
      }
      verify(eventWriter, times(1)).end();
    }

    @Test public void shouldNotSendMetricBufferSizeGreaterThenMax() throws IOException {
      setUpCustomMetrics(measurementBuffer, 10);
      fillBuffer(measurementBuffer, MeasurementBuffer.SIZE);

      sender.send(513);

      verify(eventWriter, never()).begin();
      verify(eventWriter, never()).write(any(Measurement.class), (String) any());
      verify(eventWriter, never()).write(any(Metric.class));
      verify(eventWriter, never()).end();

    }

    @Test public void shouldSendMeasurementsGreaterEndTime() throws IOException {
      setUp10CustomMeasurementLesserEndTime(measurementBuffer);
      sender.send(0);
      verify(eventWriter, never()).begin();
      verify(eventWriter, never()).write(any(Measurement.class), (String) any());
      verify(eventWriter, never()).write(any(Metric.class));
      verify(eventWriter, never()).end();
    }

    @Test public void shouldNotSendMeasurementsAndMetric() throws IOException {
      setUp10CustomMeasurementAndMetricLesserEndTime(measurementBuffer);
      current.metric.set((Metric) measurementBuffer.at(3).a);
      sender.send(0);
      verify(eventWriter, times(1)).begin();
      verify(eventWriter, times(1)).write(any(Measurement.class), (String) isNotNull());
      verify(eventWriter, times(1)).end();
    }

    @Test public void shouldNotSendMetricLesserThanMaxTime() throws IOException {
      setUp10CustomMetricLesserThanMaxTime(measurementBuffer);
      current.metric.set((Metric) measurementBuffer.at(3).a);
      sender.send(0);
      verify(eventWriter, never()).begin();
      verify(eventWriter, never()).write(any(Measurement.class), (String) any());
      verify(eventWriter, never()).write(any(Metric.class));
      verify(eventWriter, never()).end();
    }

    @Test public void shouldNotSendMeasurementsLesserThanMaxTime() throws IOException {
      setUp10CustomMeasurementLesserThanMaxTime(measurementBuffer);
      sender.send(0);
      verify(eventWriter, never()).begin();
      verify(eventWriter, never()).write(any(Measurement.class), (String) any());
      verify(eventWriter, never()).write(any(Metric.class));
      verify(eventWriter, never()).end();
    }

    @Test public void shouldOnlySendWithAtLeast10EntriesInTheBuffer() throws IOException {
      setUpCustomMeasurements(measurementBuffer, 8);
      int startIndex = 0;
      int nextStartIndex = sender.send(startIndex);
      assertThat(nextStartIndex).isEqualTo(startIndex);
      verify(eventWriter, never()).begin();

      setUpCustomMeasurements(measurementBuffer, 2);
      nextStartIndex = sender.send(startIndex);
      assertThat(nextStartIndex).isGreaterThan(startIndex);
      verify(eventWriter).begin();
      verify(eventWriter, atLeastOnce()).write(any(Measurement.class), (String) any());
      verify(eventWriter).end();
    }

    @Test public void shouldSendIfBufferIsFull() throws IOException {
      setUpCustomMetrics(measurementBuffer, 10);
      int startIndex = 0;
      startIndex = sender.send(startIndex);
      setupFullBuffer(measurementBuffer);
      clearInvocations(eventWriter);

      sender.send(startIndex);

      verify(eventWriter).begin();
      verify(eventWriter, times(MeasurementBuffer.SIZE))
          .write(any(Measurement.class), (String) any());
      verify(eventWriter).end();
    }

    @Test public void shouldReturnNextStartIndex() throws IOException {
      setUpCustomMetrics(measurementBuffer, 10);
      int startIndex = 1;
      int nextStartIndex = sender.send(startIndex);

      clearInvocations(eventWriter);

      setUpCustomMetrics(measurementBuffer, 10);

      for (int i = startIndex; i < nextStartIndex; i++) { // all previous measurement are cleared
        assertThat(measurementBuffer.at(i)).is(cleared());
      }

      int endIndex = nextStartIndex + measurementBuffer.count(nextStartIndex);
      for (int i = nextStartIndex; i < endIndex; i++) {
        assertThat(measurementBuffer.at(i)).isNot(cleared()); // new measurements
      }

      sender.send(nextStartIndex);

      verify(eventWriter).begin();
      verify(eventWriter, times(10)).write(any(Metric.class));
      verify(eventWriter).end();

      for (int i = startIndex; i < endIndex; i++) {
        assertThat(measurementBuffer.at(i)).is(cleared()); // all previous measurement are cleared
      }
    }
  }

  public static class DebugIsNullSendingBehaviorSpec extends SenderSpec {
    @Before public void initSender() {
      sender = new Sender(measurementBuffer, current, eventWriter, null, true);
    }

    @Test public void shouldNotFailSendingMeasurement() throws IOException {
      setUpCustomMeasurements(measurementBuffer, 10);
      sender.send(0);
      verify(eventWriter, times(1)).begin();
      verify(eventWriter, times(10)).write(any(Measurement.class), (String) isNull());
      verify(eventWriter, times(1)).end();
    }


    @Test public void shouldNotFailSendingMetric() throws IOException {
      setUpCustomMetrics(measurementBuffer, 10);
      sender.send(0);
      verify(eventWriter, times(1)).begin();
      verify(eventWriter, times(9)).write(any(Metric.class));
      verify(eventWriter, times(1)).end();
    }
  }

  public static class DisabledSendingMeasurementsWithoutMetricBehaviorSpec extends SenderSpec {
    @Before
    public void initSender() {
      sender = new Sender(measurementBuffer, current, eventWriter, new Debug(), false);
    }

    @Test public void shouldNotSendMeasurementsWithoutAMetric() throws IOException {
      setUpCustomMeasurements(measurementBuffer, 10);

      sender.send(0);

      verify(eventWriter, never()).begin();
    }

    @Test public void shouldSendMeasurementsWithAMetric() throws IOException {
      setUpCustomMetrics(measurementBuffer, 1);
      setUpCustomMeasurements(measurementBuffer, 10);

      sender.send(0);

      verify(eventWriter, times(10)).write((Measurement) any(), (String) any());
    }
  }

  /* setup test fixtures */

  protected void setupFullBuffer(MeasurementBuffer buffer) {
    for (Measurement next = buffer.next(); next != null; next = buffer.next()) {
      next.type = Measurement.CUSTOM;
      next.a = "custom-measurement";
      next.startTime = 0L;
      next.endTime = 999 * 1000000L;
    }
  }

  protected void setUpCustomMeasurements(MeasurementBuffer measurementBuffer, int count) {
    for (int i = 0; i < count; i++) {
      Measurement measurement = measurementBuffer.next();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = 0L;
      measurement.endTime = 999 * 1000000L;
    }
  }

  protected void setUp10CustomMeasurementLesserThanMaxTime(MeasurementBuffer measurementBuffer) {
    for (int i = 0; i < 10; i++) {
      Measurement measurement = measurementBuffer.next();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = System.currentTimeMillis();
      measurement.endTime = 0L;
    }
  }

  protected void setUp10CustomMeasurementLesserEndTime(MeasurementBuffer measurementBuffer) {
    for (int i = 0; i < 10; i++) {
      Measurement measurement = measurementBuffer.next();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = 999 * 1000000L;
      measurement.endTime = 0L;
    }
  }

  protected void setUp10CustomMeasurementAndMetricLesserEndTime(MeasurementBuffer measurementBuffer) {
    for (int i = 0; i < 10; i++) {
      Measurement measurement = measurementBuffer.next();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = 999 * 1000000L;
      measurement.endTime = 1L;
      if (i == 2) {
        measurement.type = Measurement.METRIC;
        Metric metric = new Metric();
        metric.id = "custom-metric";
        measurement.startTime = 0L;
        measurement.endTime = 999 * 1000000L;
        measurement.a = metric;
      }
      if (i == 3) {
        measurement.type = Measurement.URL;
        Metric metric = new Metric();
        metric.id = "custom-url";
        measurement.startTime = 0L;
        measurement.endTime = 999 * 1000000L;
        measurement.a = metric;
      }
    }
  }

  protected void setUp10CustomMetricLesserThanMaxTime(MeasurementBuffer measurementBuffer) {
    for (int i = 0; i < 10; i++) {
      Measurement measurement = measurementBuffer.next();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = 999 * 1000000L;
      measurement.endTime = 1L;
      if (i == 2) {
        measurement.type = Measurement.METRIC;
        Metric metric = new Metric();
        metric.id = "custom-metric";
        measurement.startTime = System.currentTimeMillis();
        measurement.endTime = 999 * 1000000L;
        measurement.a = metric;
      }
    }
  }

  protected void setUpCustomMetrics(MeasurementBuffer measurementBuffer, int count) {
    for (int i = 0; i < count; i++) {
      Measurement measurement = measurementBuffer.next();
      measurement.type = Measurement.METRIC;
      Metric metric = new Metric();
      metric.id = "custom-metric";
      metric.startTime = 0L;
      metric.endTime = 999 * 1000000L;
      measurement.a = metric;
    }
  }

  protected Condition<Measurement> cleared() {
    return new Condition<Measurement>() {
      @Override
      public boolean matches(Measurement value) {
        return value.trackingId == 0 && value.type == 0 && value.a == null
            && value.b == null && value.startTime == 0 && value.endTime == 0;
      }
    };
  }

  protected static void fillBuffer(MeasurementBuffer buffer, int count) {
    for (int i = 1; i <= count; i++) {
      buffer.next();
    }
  }
}
