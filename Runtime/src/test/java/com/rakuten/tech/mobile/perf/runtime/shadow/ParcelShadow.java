package com.rakuten.tech.mobile.perf.runtime.shadow;

import android.os.Bundle;
import android.os.Parcel;
import java.util.LinkedList;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Parcel.class)
public class ParcelShadow {

  LinkedList<Object> stack = new LinkedList<>();

  @Implementation
  public final void writeDouble(double val) {
    stack.add(val);
  }

  @Implementation public final void writeString(String val) {
    stack.add(val);
  }

  @Implementation public final void writeBundle(Bundle val) {
    stack.add(val);
  }

  @Implementation public final double readDouble() {
    return (Double) stack.remove();
  }

  @Implementation public final String readString() {
    return (String) stack.remove();
  }

  @Implementation public final Bundle readBundle() {
    return (Bundle) stack.remove();
  }
}
