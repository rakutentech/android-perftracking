package com.rakuten.tech.mobile.perf.runtime;

import static org.mockito.Mockito.verify;

import com.rakuten.tech.mobile.perf.runtime.internal.TrackingManager;
import java.util.Arrays;
import java.util.Collection;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.ParameterizedRobolectricTestRunner;

public class MetricSpec extends RobolectricUnitSpec {

  @Mock TrackingManager trackingManager;

  @Before
  public void init() {
    TrackingManager.INSTANCE = trackingManager;
  }

  @RunWith(ParameterizedRobolectricTestRunner.class)
  public static class InvalidInputSpec extends RobolectricUnitSpec {

    @ParameterizedRobolectricTestRunner.Parameters(name = "Input = {0}")
    public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {{null}, {""}, {"appQ\\"}, {"appq\""}});
    }

    private String input;

    public InvalidInputSpec(String input) {
      this.input = input;
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldValidateIdIsNotNullOnStart() {
      Metric.start(input);
    }
  }

  @Test
  public void shouldRelayStartToTrackingManager() {
    Metric.start("validId");
    verify(trackingManager).startMetric("validId");
  }

  @Test
  public void shouldNotFailOnNullTrackingManager() {
    TrackingManager.INSTANCE = null;

    try {
      Metric.start("id1");
    } catch(NullPointerException e) {
      TestCase.fail("Should not throw an Exception when TrackingManager is null.");
    }
  }

  @Test
  public void shouldStartMetricWithValidInput() {
    Metric.start("appQ1_- .");
    verify(trackingManager).startMetric("appQ1_- .");
  }

  @Test
  public void shouldRelayProlongToTrackingManager() {
    Metric.prolong();
    verify(trackingManager).prolongMetric();
  }

  @Test
  public void prolongShouldNotFailOnNullTrackingManager() {
    TrackingManager.INSTANCE = null;

    try {
      Metric.prolong();
    } catch(NullPointerException e) {
      TestCase.fail("Should not throw an Exception when TrackingManager is null.");
    }
  }

  @Test
  public void shouldEndMetric() {
    Metric.end();

    verify(trackingManager).endMetric();
  }

  @Test
  public void endMetricShouldNotFailOnNullTrackingManager() {
    TrackingManager.INSTANCE = null;

    try {
      Metric.end();
    } catch(NullPointerException e) {
      TestCase.fail("Should not throw an Exception when TrackingManager is null.");
    }
  }
}
