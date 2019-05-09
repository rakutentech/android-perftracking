package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.internal.verification.VerificationModeFactory.times;

import android.content.Context;
import com.rakuten.tech.mobile.perf.core.CachingObservable;
import com.rakuten.tech.mobile.perf.core.Config;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.core.MockTracker;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.shadow.TrackerShadow;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

@org.robolectric.annotation.Config(shadows = {TrackerShadow.class})
public class TrackingManagerSpec extends RobolectricUnitSpec {

  private TrackingManager manager;
  @Mock Context context;
  @Mock Config config;
  @Mock MockTracker tracker;
  private CachingObservable<LocationData> location = new CachingObservable<LocationData>(null);
  private CachingObservable<Float> battery = new CachingObservable<Float>(null);

  @Before
  public void init() {
    TrackingManager.initialize(
        context, config, location, battery, new AnalyticsBroadcaster(context, true));
    manager = TrackingManager.INSTANCE;
    TrackerShadow.mockTracker = tracker;
  }

  // init

  @Test
  public void shouldCreateInstanceOnInit() {
    TrackingManager.INSTANCE = null;
    TrackingManager.initialize(
        context, config, location, battery, new AnalyticsBroadcaster(context, true));
    assertThat(TrackingManager.INSTANCE).isNotNull();
  }

  @Test
  public void shouldCreateNewInstanceOnEveryInit() {
    TrackingManager previousInstance = TrackingManager.INSTANCE;
    TrackingManager.initialize(
        context, config, location, battery, new AnalyticsBroadcaster(context, true));
    assertThat(previousInstance).isNotEqualTo(TrackingManager.INSTANCE);
  }

  // measurements

  @Test
  public void shouldRelayStartMeasurementToTracker() {
    manager.startMeasurement("testId");
    verify(tracker).startCustom("testId");
  }

  @Test
  public void shouldOnlyStartMeasurementOnce() {
    manager.startMeasurement("testId");
    manager.startMeasurement("testId");
    verify(tracker, times(1)).startCustom("testId");
  }

  @Test
  public void shouldRelayEndMeasurementToTracker() {
    manager.startMeasurement("testId");
    manager.endMeasurement("testId");
    verify(tracker).endCustom(anyInt());
  }

  @Test
  public void shouldOnlyRelayEndMeasurementWhenAlreadyStarted() {
    manager.endMeasurement("testId");
    verify(tracker, never()).endCustom(anyInt());
  }

  // metrics

  @Test
  public void shouldRelayStartMetricToTracker() {
    manager.startMetric("testMetric");
    verify(tracker).startMetric("testMetric");
  }

  @Test
  public void shouldRelayEndMetricToTracker() {
    manager.startMetric("testMetric");
    manager.endMetric();
    verify(tracker).endMetric();
  }

  // aggregated

  @Test
  public void shouldStartMultipleMeasurementsForSameAggregatedId() {
    manager.startAggregated("testId", "data1");
    manager.startAggregated("testId", "data2");
    verify(tracker, times(2)).startCustom("testId");
  }

  @Test
  public void shouldRelayEndAggregatedMeasurementToTracker() {
    manager.startAggregated("testId", "config");
    manager.endAggregated("testId", "config");
    verify(tracker, times(1)).endCustom(anyInt());
  }

  @Test
  public void shouldOnRelayEndMeasurementWhenAlreadyStarted() {
    manager.endAggregated("testId", "config");
    verify(tracker, never()).endCustom(anyInt());
  }

  // many measurements

  @Test
  public void shouldNotFailOnManyParallelMeasurments() {
    for (int i = 0; i < 1000; i++) {
      manager.startMeasurement(String.valueOf(i));
    }
    for (int i = 0; i < 1000; i++) {
      manager.endMeasurement(String.valueOf(i));
    }
    verify(tracker, times(1000)).startCustom(anyString());
    // no error
  }
}
