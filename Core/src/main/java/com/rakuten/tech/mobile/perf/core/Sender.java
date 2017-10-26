package com.rakuten.tech.mobile.perf.core;

import java.io.IOException;

class Sender {

  private static final long MIN_TIME = 5L; // 5 ms
  private static final int MIN_COUNT = 10;

  private final MeasurementBuffer _buffer;
  private final Current _current;
  private final EventWriter _writer;
  private final Debug _debug;
  private Metric _metric;
  private int _sent;

  Sender(MeasurementBuffer buffer, Current current, EventWriter writer, Debug debug) {
    _buffer = buffer;
    _current = current;
    _writer = writer;
    _debug = debug;
  }

  /**
   * Tries to send measurements and metrics from the underlying buffer starting from {@code
   * startIndex}. Will wait while each measurement/metric is potentially still "alive", i.e. it
   * started less than {@link Metric#MAX_TIME} or {@link Measurement#MAX_TIME} ago respectively.
   * Returns the buffer index next unsent measurement/metric, which should be used for the next
   * call to this method as new {@code startIndex}.
   *
   * @param startIndex send measurements starting from this index (of the buffer)
   * @return next unsent index, i.e. {@code startIndex} for the next call to send
   */
  int send(int startIndex) throws IOException {
    int endIndex = _buffer.nextTrackingId.get() % MeasurementBuffer.SIZE;
    if (endIndex < 0) {
      endIndex += MeasurementBuffer.SIZE;
    }

    int count;

    if (startIndex == endIndex && _buffer.next() == null) {
      count = MeasurementBuffer.SIZE;
    } else {
      count = endIndex - startIndex;
      if (count < 0) {
        count += MeasurementBuffer.SIZE;
      }
    }

    if (count >= MIN_COUNT) {
      startIndex = send(startIndex, count);
    }

    return startIndex;
  }

  /**
   * Tries to send measurements from measurement buffer starting at {@code startIndex} until
   * {@code endIndex}. Will stop sending if the metric/measurement is potentially still "alive",
   * i.e. it started less than {@link Metric#MAX_TIME} or {@link Measurement#MAX_TIME} ago
   * respectively. Returns the buffer index next unsent measurement/metric, which should be
   * used for the next call to this method as new {@code startIndex}.
   *
   * @param startIndex send measurements starting from this index (of the buffer)
   * @param count      send `count` many measurements
   * @return last sent index + 1, i.e. {@code startIndex} for the next call to send
   */
  private int send(int startIndex, int count) throws IOException {
    _sent = 0;
    long now = System.currentTimeMillis();
    Metric _savedMetric = _metric == null ? null : _metric.copy();

    try {
      int endIndex = (startIndex + count) % MeasurementBuffer.SIZE;
      for (int i = startIndex; count > 0; count--, i = (i + 1) % MeasurementBuffer.SIZE) {
        Measurement m = _buffer.at[i];

        if (m.type == Measurement.METRIC) {

          if (_metric != null) {
            send(_metric);
            _metric = null;
          }

          Metric metric = (Metric) m.a;

          if (metric == _current.metric.get()) {
            if (now - m.startTime < Metric.MAX_TIME) {
              return i;
            }

            _current.metric.compareAndSet(metric, null);
          }

          _metric = metric;
          m.clear();
        } else {
          if (m.endTime == 0) {
            if (now - m.startTime < Measurement.MAX_TIME) {
              return i;
            }

            m.clear();
            continue;
          }

          if ((_metric != null) && (m.startTime > _metric.endTime)) {
            send(_metric);
            _metric = null;
          }

          if ((_metric != null) && (m.type == Measurement.URL)) {
            _metric.urls++;
          }

          send(m, _metric != null ? _metric.id : null);
          m.clear();
        }
      }

      return endIndex;
    } catch (IOException sendFailed) {
            /*
             * If the sending fails somewhere in the middle of the loop SenderThread will try to
             * resend the same buffer range again later. So we restore the previously active metric
             * in case it was overwritten during the loop
             */
      _metric = _savedMetric;
      throw sendFailed;
    } finally {
      if (_sent > 0) {
        _writer.end();
      }
    }
  }

  private void send(Metric metric) throws IOException {
    if (metric.endTime - metric.startTime < MIN_TIME) {
      return;
    }

    if (_debug != null) {
      _debug.log("SEND_METRIC", metric);
    }

    if (_sent == 0) {
      _writer.begin();
    }

    _writer.write(metric);
    _sent++;
  }

  private void send(Measurement m, String metricId) throws IOException {
    if (m.endTime - m.startTime < MIN_TIME) {
      return;
    }

    if (_debug != null) {
      _debug.log("SEND", m, metricId);
    }

    if (_sent == 0) {
      _writer.begin();
    }

    _writer.write(m, metricId);

    _sent++;
  }
}
