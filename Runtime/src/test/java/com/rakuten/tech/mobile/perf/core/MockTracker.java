package com.rakuten.tech.mobile.perf.core;

/**
 * Link time injected mock to detect calls to package private methods of {@code TrackerImpl} in the
 * Core module.
 */
public class MockTracker extends TrackerImpl {

  private MockTracker(MeasurementBuffer measurementBuffer, Current current, Debug debug, boolean shouldEnableNonMetricMeasurement) {
    super(measurementBuffer, current, debug, shouldEnableNonMetricMeasurement);
  }

  @Override
  public void startMetric(String metricId) {
  }

  @Override
  public void prolongMetric() {
  }

  @Override
  public void endMetric() {
  }

  @Override
  public void endMethod(int trackingId) {
  }

  public void endUrl(int trackingId) {
  }

  @Override
  public void endCustom(int trackingId) {
  }

  public void endMeasurement(int trackingId) {
  }

  private int count = 0;

  @Override
  public int startMethod(Object object, String method) {
    return count++;
  }

  @Override
  public int startUrl(Object url, String verb) {
    return count++;
  }

  @Override
  public int startCustom(String measurementId) {
    return count++;
  }
}

