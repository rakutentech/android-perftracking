package com.rakuten.tech.mobile.perf.core;

import java.util.concurrent.atomic.AtomicInteger;

class MeasurementBuffer {

  // Size must by a power of 2 so that Integer.MAX_VALUE is divisible by it
  static final int SIZE = 512;

  final Measurement[] at = new Measurement[SIZE];
  final AtomicInteger nextTrackingId = new AtomicInteger(1);

  MeasurementBuffer() {
    for (int i = 0; i < SIZE; i++) {
      at[i] = new Measurement();
    }
  }

  Measurement next() {
    int id = nextTrackingId.getAndIncrement();

    if (id == 0) {
      // Ensure id is not zero as zero has special purpose
      id = nextTrackingId.getAndIncrement();
    }

    int index = id % SIZE;
    if (index < 0) {
      index += SIZE;
    }

    Measurement m = at[index];

    if (m.trackingId != 0) {
      nextTrackingId.getAndDecrement();
      return null;
    }

    m.trackingId = id;

    return m;
  }

  Measurement getByTrackingId(int trackingId) {
    int index = trackingId % SIZE;
    if (index < 0) {
      index += SIZE;
    }

    Measurement m = at[index];
    if (m.trackingId == trackingId) {
      return m;
    }

    return null;
  }
}
