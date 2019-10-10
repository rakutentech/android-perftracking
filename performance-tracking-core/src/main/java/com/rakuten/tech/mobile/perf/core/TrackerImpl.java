package com.rakuten.tech.mobile.perf.core;

class TrackerImpl {

  private String activityName = null;
  private final MeasurementBuffer measurementBuffer;
  private final Current current;
  private final Debug debug;
  private final Analytics analytics;
  private final boolean trackMeasurementWithoutMetric;

  TrackerImpl(MeasurementBuffer measurementBuffer, Current current, Debug debug,
      Analytics analytics,
      boolean trackMeasurementWithoutMetric) {
    this.measurementBuffer = measurementBuffer;
    this.current = current;
    this.debug = debug;
    this.analytics = analytics;
    this.trackMeasurementWithoutMetric = trackMeasurementWithoutMetric;
  }

  void updateActivityName(String name) {
    this.activityName = name;
  }

  void clearActivityName(String name) {
    if (activityName != null && activityName.equals(name)) {
      updateActivityName(null);
    }
  }

  void startMetric(String metricId) {
    Metric metric = new Metric();
    metric.id = metricId;
    current.metric.set(metric);

    Measurement m = startMeasurement(Measurement.METRIC, metric, null);
    if (m == null) {
      current.metric.set(null);
      return;
    }

    metric.startTime = m.startTime;
    metric.endTime = m.startTime;

    if (debug != null) {
      debug.log("METRIC_START", metric);
    }
  }

  void prolongMetric() {
    Metric metric = current.metric.get();

    if (metric != null) {
      long now = System.currentTimeMillis();
      metric.endTime = now;
      if (now - metric.startTime > Metric.maxTime()) {
        current.metric.compareAndSet(metric, null);
      }

      if (debug != null) {
        debug.log("METRIC_PROLONG", metric);
      }
    }
  }

  void endMetric() {
    if (debug != null) {
      Metric metric = current.metric.get();
      if (metric != null) {
        debug.log("METRIC_END", metric);
      }
    }

    current.metric.set(null);
  }

  public int startMethod(Object object, String method) {
    if ((object != null) && (method != null)) {
      Measurement m = startMeasurement(Measurement.METHOD, object.getClass().getName(), method);
      if (m != null) {

        if (debug != null) {
          debug.log("METHOD_START", m, null);
        }

        return m.trackingId;
      }
    }
    return 0;
  }

  public void endMethod(int trackingId) {
    Measurement m = endMeasurement(trackingId);

    if (debug != null && m != null) {
      debug.log("METHOD_END", m, null);
    }
  }

  int startUrl(Object url, String verb) {
    if (url != null) {
      Measurement m = startMeasurement(Measurement.URL, url, verb);
      if (m != null) {

        if (debug != null) {
          debug.log("URL_START", m, null);
        }

        return m.trackingId;
      }
    }
    return 0;
  }

  void endUrl(int trackingId, int statusCode, String cdnHeader, long contentLength) {
    Measurement m = null;
    if (statusCode != 0) {
      m = endMeasurement(trackingId, statusCode);
    } else {
      m = endMeasurement(trackingId);
    }

    if(m != null) {
      analytics.sendUrlMeasurement(m, cdnHeader, contentLength);
    }

    if (debug != null) {
      if (m != null) {
        debug.log("URL_END", m, null);
      }
    }
  }

  int startCustom(String measurementId) {
    if (measurementId != null) {
      Measurement m = startMeasurement(Measurement.CUSTOM, measurementId, null);
      if (m != null) {

        if (debug != null) {
          debug.log("CUSTOM_START", m, null);
        }

        return m.trackingId;
      }
    }
    return 0;
  }

  void endCustom(int trackingId) {
    Measurement m = endMeasurement(trackingId);

    if (debug != null && m != null) {
      debug.log("CUSTOM_END", m, null);
    }
  }

  private Measurement startMeasurement(byte type, Object a, Object b) {
    if (!trackMeasurementWithoutMetric && current.metric.get() == null) {
      return null;
    }

    Measurement m = measurementBuffer.next();
    if (m == null) {
      return null;
    }

    m.type = type;
    m.a = a;
    m.b = b;
    m.startTime = System.currentTimeMillis();
    m.activityName = activityName;

    return m;
  }

  private Measurement endMeasurement(int trackingId) {
    return endMeasurement(trackingId, null);
  }

  private Measurement endMeasurement(int trackingId, Object c) {
    if (trackingId != 0) {
      Measurement m = measurementBuffer.getByTrackingId(trackingId);
      if (m != null) {
        m.endTime = System.currentTimeMillis();
        m.c = c;
      }
      return m;
    }
    return null;
  }
}
