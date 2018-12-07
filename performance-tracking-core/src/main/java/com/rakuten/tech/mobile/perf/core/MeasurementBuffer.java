package com.rakuten.tech.mobile.perf.core;

import java.util.concurrent.atomic.AtomicInteger;

class MeasurementBuffer {

  // Size must by a power of 2 so that Integer.MAX_VALUE is divisible by it
  static final int SIZE = 512;

  private final Measurement[] measurements = new Measurement[SIZE];
  // Tracking id shouldn't be zero as zero has special purpose
  private final AtomicInteger nextTrackingId = new AtomicInteger(1);

  MeasurementBuffer() {
    for (int i = 0; i < SIZE; i++) {
      measurements[i] = new Measurement();
    }
  }

  // Constructor for testing only
  MeasurementBuffer(int firstTrackingId) {
    this();
    nextTrackingId.set(firstTrackingId);
  }

  /**
   * Retrieves measurement at the provided {@code index}
   *
   * @param index to retrieve
   * @return the measurement for the provided index
   */
  Measurement at(int index) {
    return measurements[index];
  }

  /**
   * Adds the next tracking id to the next buffer slot and returns the measurement for that slot
   *
   * @return the next measurement or null if the buffer is full
   */
  Measurement next() {
    int id = nextTrackingId.getAndIncrement();
    int nextIndex = indexForId(id);

    if (at(nextIndex).trackingId != 0) {
      nextTrackingId.getAndDecrement();
      return null;
    }

    Measurement measurement = measurements[nextIndex];
    measurement.trackingId = id;

    return measurement;
  }

  /**
   * Retrieves a measurement by tracking Id
   *
   * @param trackingId to retrieve
   * @return measurement with the provided tracking id, or null if the tracking id doesn't exist
   */
  Measurement getByTrackingId(int trackingId) {
    Measurement measurement = measurements[indexForId(trackingId)];
    if (measurement.trackingId == trackingId) {
      return measurement;
    }

    return null;
  }

  /**
   * Returns the count of measurements in the buffer from {@code startIndex}
   *
   * @param startIndex to count from
   * @return count of measurements
   */
  int count(int startIndex) {
    int id = nextTrackingId.get();
    int endIndex = indexForId(id);

    int count;

    if (startIndex == endIndex && at(endIndex).trackingId != 0) {
      count = SIZE;
    } else {
      count = endIndex - startIndex;
      if (count < 0) {
        count += SIZE;
      }
    }

    return count;
  }

  private static int indexForId(int id) {
    int index = id % SIZE;

    if (index < 0) {
      index += SIZE;
    }

    return index;
  }
}
