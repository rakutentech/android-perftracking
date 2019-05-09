package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.util.Log;
import com.rakuten.tech.mobile.perf.core.Analytics;
import com.rakuten.tech.mobile.perf.core.CachingObservable;
import com.rakuten.tech.mobile.perf.core.Config;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.core.Tracker;
import java.util.HashMap;
import java.util.Map;

/** TrackingManager */
public class TrackingManager {

  private static final String TAG = TrackingManager.class.getSimpleName();
  public static TrackingManager INSTANCE = null;
  /** maps {@link TrackingData} to Measurement ids (from {@link Tracker} */
  private Map<TrackingData, Integer> trackingData;
  /* Max number of objects for @TrackingManager#trackingData */
  private static final int TRACKING_DATA_LIMIT = 100;

  private TrackingManager() {
    trackingData = new HashMap<>();
  }

  static synchronized void initialize(
      Context context,
      Config config,
      CachingObservable<LocationData> locationObservable,
      CachingObservable<Float> batteryInfoObservable,
      Analytics analytics) {
    Tracker.on(context, config, locationObservable, batteryInfoObservable, analytics);
    INSTANCE = new TrackingManager();
  }

  static synchronized void deinitialize() {
    Tracker.off();
    INSTANCE = null;
  }

  /**
   * Starts a new measurement.
   *
   * @param measurementId Measurement identifier.
   */
  public synchronized void startMeasurement(String measurementId) {
    startAggregated(measurementId, null);
  }

  /**
   * Ends a measurement.
   *
   * @param measurementId Measurement identifier.
   */
  public synchronized void endMeasurement(String measurementId) {
    endAggregated(measurementId, null);
  }

  /**
   * Starts a new aggregated measurement.
   *
   * @param id Measurement identifier.
   * @param object Object associated with the measurement.
   */
  public synchronized void startAggregated(String id, Comparable object) {
    TrackingData key = new TrackingData(id, object);
    if (trackingData.size() >= TRACKING_DATA_LIMIT) {
      trackingData.clear();
    }
    if (!trackingData.containsKey(key)) {
      trackingData.put(key, Tracker.startCustom(id));
    } else {
      Log.d(TAG, "Measurement already started");
    }
  }

  /**
   * Ends a aggregated measurement.
   *
   * @param id Measurement identifier.
   * @param object Object associated with the measurement.
   */
  public synchronized void endAggregated(String id, Comparable object) {
    TrackingData key = new TrackingData(id, object);
    if (trackingData.containsKey(key)) {
      Tracker.endCustom(trackingData.get(key));
      trackingData.remove(key);
    } else {
      Log.d(TAG, "Measurement not found");
    }
  }

  /**
   * Starts a new metric.
   *
   * @param metricId Metric ID, for example "launch", "search", "item"
   */
  public void startMetric(String metricId) {
    Tracker.startMetric(metricId);
  }

  /**
   * Ends the currently running metric.
   */
  public void endMetric() {
    Tracker.endMetric();
  }

  /** Prolongs current metric. */
  public void prolongMetric() {
    Tracker.prolongMetric();
  }
}
