package com.rakuten.tech.mobile.perf.core;

class TrackerImpl {

  private String activityName = null;
  private final MeasurementBuffer _measurementBuffer;
  private final Current _current;
  private final Debug _debug;

  TrackerImpl(MeasurementBuffer measurementBuffer, Current current, Debug debug) {
    _measurementBuffer = measurementBuffer;
    _current = current;
    _debug = debug;
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
    _current.metric.set(null);

    Metric metric = new Metric();
    metric.id = metricId;

    Measurement m = startMeasurement(Measurement.METRIC, metric, null);
    if (m != null) {
      metric.startTime = m.startTime;
      metric.endTime = m.startTime;
      _current.metric.set(metric);

      if (_debug != null) {
        _debug.log("METRIC_START", metric);
      }
    }
  }

  void prolongMetric() {
    Metric metric = _current.metric.get();

    if (metric != null) {
      long now = System.currentTimeMillis();
      metric.endTime = now;
      if (now - metric.startTime > Metric.MAX_TIME) {
        _current.metric.compareAndSet(metric, null);
      }

      if (_debug != null) {
        _debug.log("METRIC_PROLONG", metric);
      }
    }
  }

  void endMetric() {
    if (_debug != null) {
      Metric metric = _current.metric.get();
      if (metric != null) {
        _debug.log("METRIC_END", metric);
      }
    }

    _current.metric.set(null);
  }

  public int startMethod(Object object, String method) {
    if ((object != null) && (method != null)) {
      Measurement m = startMeasurement(Measurement.METHOD, object.getClass().getName(), method);
      if (m != null) {

        if (_debug != null) {
          _debug.log("METHOD_START", m, null);
        }

        return m.trackingId;
      }
    }
    return 0;
  }

  public void endMethod(int trackingId) {
    endMeasurement(trackingId);

    if (_debug != null) {
      Measurement m = _measurementBuffer.getByTrackingId(trackingId);
      if (m != null) {
        _debug.log("METHOD_END", m, null);
      }
    }
  }

  int startUrl(Object url, String verb) {
    if (url != null) {
      Measurement m = startMeasurement(Measurement.URL, url, verb);
      if (m != null) {

        if (_debug != null) {
          _debug.log("URL_START", m, null);
        }

        return m.trackingId;
      }
    }
    return 0;
  }

  void endUrl(int trackingId) {
    endMeasurement(trackingId);

    if (_debug != null) {
      Measurement m = _measurementBuffer.getByTrackingId(trackingId);
      if (m != null) {
        _debug.log("URL_END", m, null);
      }
    }
  }

  int startCustom(String measurementId) {
    if (measurementId != null) {
      Measurement m = startMeasurement(Measurement.CUSTOM, measurementId, null);
      if (m != null) {

        if (_debug != null) {
          _debug.log("CUSTOM_START", m, null);
        }

        return m.trackingId;
      }
    }
    return 0;
  }

  void endCustom(int trackingId) {
    endMeasurement(trackingId);

    if (_debug != null) {
      Measurement m = _measurementBuffer.getByTrackingId(trackingId);
      if (m != null) {
        _debug.log("CUSTOM_END", m, null);
      }
    }
  }

  private Measurement startMeasurement(byte type, Object a, Object b) {
    Measurement m = _measurementBuffer.next();
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

  private void endMeasurement(int trackingId) {
    if (trackingId != 0) {
      Measurement m = _measurementBuffer.getByTrackingId(trackingId);
      if (m != null) {
        m.endTime = System.currentTimeMillis();
      }
    }
  }
}
