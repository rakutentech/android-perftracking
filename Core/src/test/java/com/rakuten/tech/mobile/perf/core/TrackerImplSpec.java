package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class TrackerImplSpec {

  @Mock Debug debug;
  /* Spy */ private MockBuffer buffer;
  /* Spy */ private AtomicReference<Metric> metric;
  private TrackerImpl tracker;

  private class MockBuffer extends MeasurementBuffer {

    final List<Measurement> measurements = new LinkedList<>();

    @Override public Measurement next() {
      Measurement m = new Measurement();
      measurements.add(m);
      m.trackingId = measurements.indexOf(m) + 1;
      return m;
    }

    @Override
    public Measurement getByTrackingId(int trackingId) {
      boolean validId = trackingId > 0 && trackingId <= measurements.size();

      return validId ? measurements.get(trackingId - 1) : null;
    }
  }

  @Before public void initMocks() throws NoSuchFieldException, IllegalAccessException {
    MockitoAnnotations.initMocks(this);
    Current current = new Current();
    Field metricField = Current.class.getDeclaredField("metric");
    metricField.setAccessible(true);
    metric = spy(current.metric);
    buffer = spy(new MockBuffer());
    metricField.set(current, metric);
    tracker = new TrackerImpl(buffer, current, debug);
  }

  // metrics

  @Test public void shouldInitializeMetricOnStartMetric() throws InterruptedException {
    long beforeStart = System.currentTimeMillis();
    Thread.sleep(1);
    tracker.startMetric("some metric");

    assertThat(metric.get()).isNotNull();
    assertThat(metric.get().startTime).isGreaterThan(beforeStart);
    assertThat(metric.get().endTime).isGreaterThan(beforeStart);
  }

  @Test public void shouldInitializeNewMeasurementOnStartMetric() {
    tracker.startMetric("some metric");

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    assertThat(buffer.measurements.size()).isEqualTo(1);
    Measurement m = buffer.measurements.get(0);
    assertThat(m.type).isEqualTo(Measurement.METRIC);
    assertThat(m.a).isEqualTo(metric.get());
    assertThat(m.b).isNull();
  }

  @Test public void shouldRetainNewMetricOnStartMetric() {
    tracker.startMetric("metric ID");

    ArgumentCaptor<Metric> captor = ArgumentCaptor.forClass(Metric.class);
    verify(metric, times(2)).set(captor.capture());
    assertThat(captor.getAllValues().get(0)).isNull(); // cleared old metric
    assertThat(captor.getAllValues().get(1).id).isEqualTo("metric ID"); // set new metric
  }

  @Test public void shouldOnlyRetainOneMetricAtATime() {
    tracker.startMetric("old metric");
    tracker.startMetric("new metric");

    assertThat(metric.get()).isNotNull();
    assertThat(metric.get().id).isEqualTo("new metric");
  }

  @Test public void shouldReleaseMetricOnEndMetric() {
    tracker.startMetric("metric ID");
    assertThat(metric.get()).isNotNull();
    tracker.endMetric();
    assertThat(metric.get()).isNull();
  }

  @Test public void shouldNotFailOnDoubleEndMetric() {
    tracker.endMetric();
    tracker.endMetric();
    // no exception
  }

  @Test public void shouldNotProlongMetricAfterTimeout() throws InterruptedException {
    tracker.startMetric("testMetric");
    Metric m = metric.get();
    m.startTime = m.startTime - (Metric.MAX_TIME); // pretend it is an old metric
    Thread.sleep(1);
    tracker.prolongMetric();

    assertThat(metric.get()).isNull();
  }

  @Test public void shouldUpdateEndTimeOnProlongMetric() throws InterruptedException {
    long beforeStart = System.currentTimeMillis();
    tracker.startMetric("some metric");
    Thread.sleep(100);
    tracker.prolongMetric();
    assertThat(metric.get().endTime).isGreaterThan(beforeStart + 100);
  }

  @Test public void shouldNotUpdateEndTimeOnEndMetric() throws InterruptedException {
    tracker.startMetric("some metric");
    Thread.sleep(10);
    tracker.prolongMetric();
    Metric m = this.metric.get();
    long endTimeBeforeEndMetric = m.endTime;
    Thread.sleep(10);
    tracker.endMetric();

    assertThat(m.endTime).isEqualTo(endTimeBeforeEndMetric);
  }

  // method measurements

  @Test public void shouldInitializeNewMethodMeasurement() {
    tracker.startMethod(new Object(), "testMethod");

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.type).isEqualTo(Measurement.METHOD);
    assertThat(m.a).isEqualTo("java.lang.Object");
    assertThat(m.b).isEqualTo("testMethod");
  }

  @Test public void shouldNotCreateMeasurementOnNullInput() {
    tracker.startMethod(null, "someMethod");
    tracker.startMethod(new Object(), null);
    tracker.startMethod(null, null);

    verify(buffer, never()).next();
  }

  @Test public void shouldUpdateExistingMethodMeasurement() throws InterruptedException {
    int id = tracker.startMethod(new Object(), "testMethod");
    Thread.sleep(10);
    long beforeEnd = System.currentTimeMillis();
    tracker.endMethod(id);

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.startTime).isLessThan(beforeEnd);
    assertThat(m.endTime).isGreaterThanOrEqualTo(beforeEnd);
  }

  // url measurements

  @Test public void shouldInitializeNewUrlMeasurement() throws MalformedURLException {
    URL url = new URL("https://rakuten.co.jp");
    tracker.startUrl(url, "GET");

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.type).isEqualTo(Measurement.URL);
    assertThat(m.a).isEqualTo(url);
    assertThat(m.b).isEqualTo("GET");
  }

  @Test public void shouldUpdateExistingUrlMeasurement()
      throws InterruptedException, MalformedURLException {
    int id = tracker.startUrl(new URL("https://rakuten.co.jp"), "GET");
    Thread.sleep(10);
    long beforeEnd = System.currentTimeMillis();
    tracker.endUrl(id);

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.startTime).isLessThan(beforeEnd);
    assertThat(m.endTime).isGreaterThanOrEqualTo(beforeEnd);
  }

  @Test public void shouldNotCreateUrlMeasurementOnNullInput() {
    tracker.startUrl(null, "GET");
    tracker.startUrl(null, null);

    verify(buffer, never()).next();
  }

  @Test public void shouldCreateUrlMeasurementWithoutVerb() throws MalformedURLException {
    URL url = new URL("https://rakuten.co.jp");
    tracker.startUrl(url, null);

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.type).isEqualTo(Measurement.URL);
    assertThat(m.a).isEqualTo(url);
    assertThat(m.b).isEqualTo(null);
  }

  // custom measurements

  @Test public void shouldInitializeNewCustomMeasurement() {
    tracker.startCustom("testId");

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.type).isEqualTo(Measurement.CUSTOM);
    assertThat(m.a).isEqualTo("testId");
  }

  @Test public void shouldUpdateExistingCustomMeasurement() throws InterruptedException {
    int id = tracker.startCustom("testId");
    Thread.sleep(10);
    long beforeEnd = System.currentTimeMillis();
    tracker.endCustom(id);

    verify(buffer, times(1)).next();
    assertThat(buffer.measurements).isNotEmpty();
    Measurement m = buffer.measurements.get(0);
    assertThat(m.startTime).isLessThan(beforeEnd);
    assertThat(m.endTime).isGreaterThanOrEqualTo(beforeEnd);
  }

  @Test public void shouldNotCreateCustomMeasurementOnNullInput() {
    tracker.startCustom(null);

    verify(buffer, never()).next();
  }

  // unexpected collaborator behavior

  @Test public void shouldNotFailWhenBufferDoesNotProvideMeasurements()
      throws MalformedURLException {
    when(buffer.next()).thenReturn(null);
    tracker.startCustom("testId");
    tracker.startMethod(new Object(), "methodName");
    tracker.startMetric("myMetric");
    tracker.startUrl(new URL("https://rakuten.co.jp"), "GET");

    // no exception
  }

  @Test public void shouldNotFailWhenBufferDoesNotLookupMeasurements() throws
      MalformedURLException {
    when(buffer.getByTrackingId(anyInt())).thenReturn(null);
    tracker.startMetric("myMetric");
    tracker.endMetric();
    int id = tracker.startCustom("testId");
    tracker.endCustom(id);
    id = tracker.startMethod(new Object(), "methodName");
    tracker.endMethod(id);
    id = tracker.startUrl(new URL("https://rakuten.co.jp"), "GET");
    tracker.endUrl(id);

    verify(buffer, times(4)).next();
    // no exception
  }

  @Test public void shouldNotFailWithNullDebug() throws MalformedURLException {
    tracker = new TrackerImpl(buffer, new Current(), null);
    tracker.startMetric("m");
    tracker.prolongMetric();
    tracker.endMetric();
    int id = tracker.startMethod(new Object(), "method");
    tracker.endMethod(id);
    id = tracker.startUrl(new URL("https://rakuten.co.jp"), "GET");
    tracker.endUrl(id);
    id = tracker.startCustom("custom");
    tracker.endCustom(id);

    // no exception
  }

  // unexpected input

  @Test public void shouldNotFailOnIncorrectProlongOrEnd() {
    tracker.prolongMetric();
    tracker.endMetric();
    tracker.endCustom(0);
    tracker.endMethod(1);
    tracker.endUrl(2);

    // no exception
  }
}
