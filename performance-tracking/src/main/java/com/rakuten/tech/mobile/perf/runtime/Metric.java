package com.rakuten.tech.mobile.perf.runtime;

import android.util.Log;
import com.rakuten.tech.mobile.perf.runtime.internal.TrackingManager;

/** Metric */
public final class Metric {

  private static final String TAG = Metric.class.getSimpleName();

  /**
   * Starts a new metric.
   *
   * @param id Metric identifier. Valid Arguments are AlphaNumeric, -, _, . and <i>Space</i>.
   */
  public static void start(String id) {
    if (Validation.isInvalidId(id)) {
      throw new IllegalArgumentException("Illegal Arguments");
    }
    if (TrackingManager.INSTANCE != null) {
      TrackingManager.INSTANCE.startMetric(id);
    } else {
      Log.d(TAG, "Tracking manager not initialized");
    }
  }

  /**
   * Ends the metric.
   */
  public static void end() {
    if (TrackingManager.INSTANCE != null) {
      TrackingManager.INSTANCE.endMetric();
    } else {
      Log.d(TAG, "Tracking manager not initialized");
    }
  }

  /**
   * Prolongs current metric.
   *
   * <p>By default the current metric is prolonged by UI life cycle events (Activity, Fragment and
   * Webview). In case you start a metric and there are no lifecycle events after starting it your
   * metric will not be recorded (the minimum duration for a metric is 5 milliseconds).
   *
   * <p>So all metrics which are started after `Activity#OnCreate` or `Fragment#onCreateView` and
   * end before the respective `onDestroy` lifecycle events need to be prolonged by calling
   * `Metric#prolong`.
   *
   * <p>In parallel execution scenarios (e.g. multiple image download) the metric should be
   * prolonged in each individual execution in order to measure the total download time.
   */
  public static void prolong() {
    if (TrackingManager.INSTANCE != null) {
      TrackingManager.INSTANCE.prolongMetric();
    } else {
      Log.d(TAG, "Tracking manager not initialized");
    }
  }
}
