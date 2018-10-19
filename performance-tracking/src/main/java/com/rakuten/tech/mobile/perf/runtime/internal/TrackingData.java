package com.rakuten.tech.mobile.perf.runtime.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

class TrackingData implements Comparable<TrackingData> {

  private final String measurementId;
  private final Comparable object;

  TrackingData(@NonNull String measurementId, @Nullable Comparable object) {
    this.measurementId = measurementId;
    this.object = object;
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof TrackingData) {
      TrackingData data = (TrackingData) o;
      return measurementId.equals(data.measurementId) && nullSafeEquateObjects(object, data.object);
    } else {
      return false;
    }
  }

  private boolean nullSafeEquateObjects(Comparable one, Comparable two) {
    if (one != null && two != null) {
      return one.equals(two);
    }
    return one == null && two == null;
  }

  @Override
  public int hashCode() {
    if (object != null) {
      return measurementId.hashCode() + object.hashCode();
    } else {
      return measurementId.hashCode();
    }
  }

  @SuppressWarnings("ConstantConditions")
  @Override
  public int compareTo(TrackingData another) {
    if (another != null) {
      if (measurementId.compareTo(another.measurementId) == 0) {
        return nullSafeCompareObjects(object, another.object);
      } else {
        return measurementId.compareTo(another.measurementId);
      }
    }
    return -1;
  }

  @SuppressWarnings("unchecked")
  private int nullSafeCompareObjects(Comparable one, Comparable two) {
    if (one == null && two == null) {
      return 0;
    }
    if (one == null ^ two == null) {
      return (one == null) ? -1 : 1;
    }
    return one.compareTo(two);
  }
}
