package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.util.Log;
import com.rakuten.tech.mobile.perf.core.CachingObservable;
import com.rakuten.tech.mobile.perf.core.Config;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.core.Tracker;
import java.util.HashMap;
import java.util.Map;

/**
 * TrackingManager
 */
public class TrackingManager {

  private static final String TAG = TrackingManager.class.getSimpleName();
  public static TrackingManager INSTANCE = null;
  /**
   * maps {@link TrackingData} to Measurement ids (from {@link Tracker}
   */
  private Map<TrackingData, Integer> mTrackingData;
  /* Max number of objects for @TrackingManager#mTrackingData */
  private static final int TRACKING_DATA_LIMIT = 100;

  private TrackingManager() {
    mTrackingData = new HashMap<>();
  }

  synchronized static void initialize(Context context, Config config,
      CachingObservable<LocationData> locationObservable) {
    Tracker.on(context, config, locationObservable);
    INSTANCE = new TrackingManager();
  }

  synchronized static void deinitialize() {
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
   * @param id     Measurement identifier.
   * @param object Object associated with the measurement.
   */
  public synchronized void startAggregated(String id, Comparable object) {
    TrackingData key = new TrackingData(id, object);
    if (mTrackingData.size() >= TRACKING_DATA_LIMIT) {
      mTrackingData.clear();
    }
    if (!mTrackingData.containsKey(key)) {
      mTrackingData.put(key, Tracker.startCustom(id));
    } else {
      Log.d(TAG, "Measurement already started");
    }
  }

  /**
   * Ends a aggregated measurement.
   *
   * @param id     Measurement identifier.
   * @param object Object associated with the measurement.
   */
  public synchronized void endAggregated(String id, Comparable object) {
    TrackingData key = new TrackingData(id, object);
    if (mTrackingData.containsKey(key)) {
      Tracker.endCustom(mTrackingData.get(key));
      mTrackingData.remove(key);
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
   * Prolongs current metric.
   *
   */
  public void prolongMetric() {
    Tracker.prolongMetric();
  }

}
