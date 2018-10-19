package com.rakuten.tech.mobile.perf.core;

import android.content.Context;
import java.net.URL;

/**
 * Tracker
 *
 * Abstracts and encapsulates low level performance tracking functionality.
 * Intended to be used from instrumented code and Analytics SDK.
 * Not intended to be used directly from app.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class Tracker {

  private static TrackerImpl _tracker;
  private static SenderThread _senderThread;

  /**
   * Turns performance tracking on.
   * @param context Instance of android.content.Context.
   * @param config Performance tracking configuration.
   */
  public static synchronized void on(Context context, Config config,
      CachingObservable<LocationData> locationObservable,
      CachingObservable<Float> batteryInfoObservable,
      Analytics analytics) {
    Debug debug = config.debug ? new Debug() : null;
    MeasurementBuffer buffer = new MeasurementBuffer();
    Current current = new Current();
    _tracker = new TrackerImpl(buffer, current, debug, analytics, config.enableNonMetricMeasurement);
    EnvironmentInfo envInfo = new EnvironmentInfo(context, locationObservable, batteryInfoObservable);
    EventWriter writer = new EventWriter(config, envInfo);
    Sender sender = new Sender(buffer, current, writer, debug, config.enableNonMetricMeasurement);
    _senderThread = new SenderThread(sender);
    _senderThread.start();
  }

  /**
   * Turns performance tracking off.
   */
  public static synchronized void off() {
    _tracker = null;
    SenderThread s = _senderThread;
    if (s != null) {
      s.terminate();
    }
    _senderThread = null;
  }

  /**
   * Returns performance tracking status.
   */
  public static boolean isTrackerRunning() {
    return _tracker != null;
  }

  /**
   * Starts metric.
   * @param metricId Metric ID, for example "launch", "search", "item".
   */
  public static void startMetric(String metricId) {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.startMetric(metricId);
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Prolongs current metric.
   */
  public static void prolongMetric() {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.prolongMetric();
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Terminates current metric.
   */
  public static void endMetric() {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.endMetric();
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Starts method measurement.
   * @param object Class instance
   * @param method Method name
   * @return Tracking ID
   */
  public static int startMethod(Object object, String method) {
    try {
      TrackerImpl t = _tracker;
      return t != null ? t.startMethod(object, method) : 0;
    } catch (Throwable t) {
      Tracker.off();
    }
    return 0;
  }

  /**
   * Ends method measurement.
   * @param trackingId Tracking ID returned from startMethod
   */
  public static void endMethod(int trackingId) {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.endMethod(trackingId);
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Starts URL measurement.
   * @param url URL
   * @param verb Verb, for example GET, POST, DELETE, or null
   * @return Tracking ID
   */
  public static int startUrl(URL url, String verb) {
    try {
      TrackerImpl t = _tracker;
      return t != null ? t.startUrl(url, verb) : 0;
    } catch (Throwable t) {
      Tracker.off();
    }
    return 0;
  }

  /**
   * Starts URL measurement.
   * @param url String
   * @param verb Verb, for example GET, POST, DELETE, or null
   * @return Tracking ID
   */
  public static int startUrl(String url, String verb) {
    try {
      TrackerImpl t = _tracker;
      return t != null ? t.startUrl(url, verb) : 0;
    } catch (Throwable t) {
      Tracker.off();
    }
    return 0;
  }

  /**
   * Ends URL measurement.
   * @param trackingId Tracking ID returned from startUrl
   * @param statusCode Response status code
   * @param cdnHeader nullable value of the X-CDN-Served-From header
   */
  public static void endUrl(int trackingId, int statusCode, String cdnHeader) {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.endUrl(trackingId, statusCode, cdnHeader);
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Starts custom measurement.
   * @param measurementId Measurement ID
   * @return Tracking ID
   */
  public static int startCustom(String measurementId) {
    try {
      TrackerImpl t = _tracker;
      return t != null ? t.startCustom(measurementId) : 0;
    } catch (Throwable t) {
      Tracker.off();
    }
    return 0;
  }

  /**
   * Ends custom measurement.
   * @param trackingId Tracking ID returned from startCustom
   */
  public static void endCustom(int trackingId) {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.endCustom(trackingId);
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Updates current activity name.
   * @param name activity name
   */
  public static void updateActivityName(String name) {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.updateActivityName(name);
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }

  /**
   * Clears current activity name when we are in same activity.
   * @param name activity name
   */
  public static void clearActivityName(String name) {
    try {
      TrackerImpl t = _tracker;
      if (t != null) {
        t.clearActivityName(name);
      }
    } catch (Throwable t) {
      Tracker.off();
    }
  }
}