package com.rakuten.tech.mobile.perf.core;

/**
 * Link time injected mock to detect calls to package private methods of {@code TrackerImpl} in the
 * performance-tracking-core module.
 */
public class MockTracker extends TrackerImpl {

  private MockTracker(MeasurementBuffer measurementBuffer, Current current, Debug debug, boolean shouldEnableNonMetricMeasurement) {
    super(measurementBuffer, current, debug, shouldEnableNonMetricMeasurement);
  }

  public void startMetric(String metricId) {
  }

  public void prolongMetric() {
  }

  public void endMetric() {
  }

  public void endMethod(int trackingId) {
  }

  public void endUrl(int trackingId) {
  }

  public void endCustom(int trackingId) {
  }

  public void endMeasurement(int trackingId) {
  }

  private int count = 0;

  public int startMethod(Object object, String method) {
    return count++;
  }

  public int startUrl(Object url, String verb) {
    return count++;
  }

  public int startCustom(String measurementId) {
    return count++;
  }
}

