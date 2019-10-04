package com.rakuten.tech.mobile.perf.core;

import java.io.IOException;

class Sender {

  private static final long MIN_TIME = 5L; // 5 ms
  private static final int MIN_COUNT = 10;


  private final MeasurementBuffer buffer;
  private final Current current;
  private final EventWriter writer;
  private final Debug debug;
  private Metric metric;
  private int sent;
  private final boolean sendMeasurementWithoutMetric;

  Sender(MeasurementBuffer buffer, Current current, EventWriter writer,
      Debug debug, boolean sendMeasurementWithoutMetric) {
    this.buffer = buffer;
    this.current = current;
    this.writer = writer;
    this.debug = debug;
    this.sendMeasurementWithoutMetric = sendMeasurementWithoutMetric;
  }

  /**
   * Tries to send measurements and metrics from the underlying buffer starting from {@code
   * startIndex}. Will wait while each measurement/metric is potentially still "alive", i.e. it
   * started less than {@link Metric#maxTime} or {@link Measurement#MAX_TIME} ago respectively.
   * Returns the index for next unsent measurement/metric, which should be used for the next
   * call to this method as new {@code startIndex}.
   *
   * @param startIndex send measurements starting from this index (of the buffer)
   * @return next unsent index, i.e. {@code startIndex} for the next call to send
   */
  int send(int startIndex) throws IOException {
    int count = buffer.count(startIndex);

    if (count >= MIN_COUNT) {
      startIndex = send(startIndex, count);
    } else {
      if (metric != null) {
        sendSingleMetric(metric);
        metric = null;
      }
    }

    return startIndex;
  }

  /**
   * Tries to send measurements from measurement buffer starting at {@code startIndex} until
   * {@code count} is reached. Will stop sending if the metric/measurement is potentially still "alive",
   * i.e. it started less than {@link Metric#maxTime} or {@link Measurement#MAX_TIME} ago
   * respectively. Returns the index for next unsent measurement/metric, which should be
   * used for the next call to this method as new {@code startIndex}.
   *
   * @param startIndex send measurements starting from this index (of the buffer)
   * @param count      send `count` many measurements
   * @return last sent index + 1, i.e. {@code startIndex} for the next call to send
   */
  private int send(int startIndex, int count) throws IOException {
    sent = 0;
    long now = System.currentTimeMillis();
    Metric savedMetric = metric == null ? null : metric.copy();

    try {
      int endIndex = (startIndex + count) % MeasurementBuffer.SIZE;
      for (int i = startIndex; count > 0; count--, i = (i + 1) % MeasurementBuffer.SIZE) {
        Measurement measurement = buffer.at(i);

        if (measurement.type == Measurement.METRIC) {
          if (metric != null) {
            send(metric);
            metric = null;
          }

          Metric measurementMetric = (Metric) measurement.a;

          if (measurementMetric == current.metric.get()) {
            if (now - measurement.startTime < Metric.maxTime()) {
              return i;
            }

            current.metric.compareAndSet(measurementMetric, null);
          }

          this.metric = measurementMetric;
          measurement.clear();
        } else {
          if (measurement.endTime == 0) {
            if (now - measurement.startTime < Measurement.MAX_TIME) {
              return i;
            }

            measurement.clear();
            continue;
          }

          if (metric != null && measurement.startTime > metric.endTime) {
            send(metric);
            metric = null;
          }

          if ((metric != null) && (measurement.type == Measurement.URL)) {
            metric.urls++;
          }

          // When `enableNonMetricMeasurements` from the config is false, measurements with
          // no metric should not even be added to the buffer. However, they occasionally are due
          // to threading issues, so this check is needed to prevent them from being sent
          if (sendMeasurementWithoutMetric || metric != null) {
            send(measurement, metric != null ? metric.id : null);
          }

          measurement.clear();
        }
      }

      return endIndex;
    } catch (IOException sendFailed) {
            /*
             * If the sending fails somewhere in the middle of the loop SenderThread will try to
             * resend the same buffer range again later. So we restore the previously active metric
             * in case it was overwritten during the loop
             */
      metric = savedMetric;
      throw sendFailed;
    } finally {
      if (sent > 0) {
        writer.end();
      }
    }
  }

  private void sendSingleMetric(Metric metric) throws IOException {
    sent = 0;
    try {
      send(metric);
    } finally {
      if (sent > 0) {
        writer.end();
      }
    }
  }

  private void send(Metric metric) throws IOException {
    if (metric.endTime - metric.startTime < MIN_TIME) {
      return;
    }

    if (debug != null) {
      debug.log("SEND_METRIC", metric);
    }

    if (sent == 0) {
      writer.begin();
    }

    writer.write(metric);
    sent++;
  }

  private void send(Measurement m, String metricId) throws IOException {
    if (m.endTime - m.startTime < MIN_TIME) {
      return;
    }

    if (debug != null) {
      debug.log("SEND", m, metricId);
    }

    if (sent == 0) {
      writer.begin();
    }

    writer.write(m, metricId);

    sent++;
  }
}
