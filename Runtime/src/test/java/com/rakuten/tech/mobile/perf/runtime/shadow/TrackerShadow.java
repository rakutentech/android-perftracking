package com.rakuten.tech.mobile.perf.runtime.shadow;

import static org.mockito.Mockito.mock;

import android.content.Context;
import com.rakuten.tech.mobile.perf.core.CachingObservable;
import com.rakuten.tech.mobile.perf.core.Config;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.core.MockTracker;
import com.rakuten.tech.mobile.perf.core.Tracker;
import java.net.URL;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Robolectric injected mock to detect calls to static functions of {@code TrackerShadow} in the
 * Core module. It mirrors the public API of {@code TrackerShadow}
 */
@Implements(Tracker.class)
public class TrackerShadow {

  public static MockTracker mockTracker = mock(MockTracker.class);

  public static MockTracker newMockTracker() {
    mockTracker = mock(MockTracker.class);
    return mockTracker;
  }

  @Implementation public static void on(Context context, Config config,
      CachingObservable<LocationData> observableLocation) { /* noop */ }

  @Implementation public static void off() { /* noop */ }

  @Implementation public static void startMetric(String metricId) {
    mockTracker.startMetric(metricId);
  }

  @Implementation public static void prolongMetric() {
    mockTracker.prolongMetric();
  }

  @Implementation public static void endMetric() {
    mockTracker.endMetric();
  }

  @Implementation public static int startMethod(Object object, String method) {
    return mockTracker.startMethod(object, method);
  }

  @Implementation public static void endMethod(int trackingId) {
    mockTracker.endMethod(trackingId);
  }

  @Implementation public static int startUrl(URL url, String verb) {
    return mockTracker.startUrl(url, verb);
  }

  @Implementation public static int startUrl(String url, String verb) {
    return mockTracker.startUrl(url, verb);
  }

  @Implementation public static void endUrl(int trackingId) {
    mockTracker.endUrl(trackingId);
  }

  @Implementation public static int startCustom(String measurementId) {
    return mockTracker.startCustom(measurementId);
  }

  @Implementation public static void endCustom(int trackingId) {
    mockTracker.endCustom(trackingId);
  }
}
