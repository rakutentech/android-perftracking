package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;

public class AnalyticsSpec {

  // capture
  Capture capture;

  private Analytics analytics = new Analytics() {
    @SuppressWarnings("unchecked")
    @Override
    public void sendEvent(String name, Map<String, ?> data) {
      capture = new Capture(name, data);
    }
  };

  private static class Capture {
    // raw
    String name;
    Map<String, ?> data;
    // internals for better test readability
    Map<String, ?> perfdata;
    Map<String, ?> entry;

    @SuppressWarnings("unchecked")
    Capture(String name, Map<String, ?> data) {
      this.name = name;
      this.data = data;

      try {
        this.perfdata = (Map<String, ?>) data.get("perfdata");
        this.entry = (Map<String, ?>) ((List) perfdata.get("entries")).get(0);
      } catch (Exception e) {
        perfdata = null;
        entry = null;
      }
    }
  }

  private Measurement measurement;

  @Before
  public void setup() throws MalformedURLException {
    measurement = new Measurement();
    measurement.startTime = 1000;
    measurement.endTime = 2000;
    measurement.a = new URL("http://example.com");
    measurement.b = "GET";
    measurement.c = 201;
  }

  // structure

  @Test
  public void shouldSendEventTypeAndPerfData() {
    analytics.sendUrlMeasurement(measurement, "akamai", 4096);

    assertThat(capture.name).isEqualTo("perf");
    assertThat(capture.data).containsKeys("perfdata");
    assertThat(capture.perfdata).containsKeys("type", "entries");
    assertThat(capture.perfdata.get("type")).isEqualTo("resource");
  }

  // mandatory fields

  @Test
  public void shouldEncodeUrlMeasurement() {
    analytics.sendUrlMeasurement(measurement, null, 0);

    assertThat(capture.entry).containsKeys("name", "startTime", "responseEnd", "duration");
    assertThat(capture.entry.get("name")).isEqualTo(measurement.a);
    assertThat(capture.entry.get("startTime")).isEqualTo(measurement.startTime);
    assertThat(capture.entry.get("responseEnd")).isEqualTo(measurement.endTime);
    assertThat(capture.entry.get("duration")).isEqualTo(measurement.endTime - measurement.startTime);
  }

  // optional fields

  @Test
  public void shouldSendPositiveContentLength() {
    analytics.sendUrlMeasurement(measurement, "akamai", 123);

    assertThat(capture.entry).containsKey("transferSize");
    assertThat(capture.entry.get("transferSize")).isEqualTo(123L);
  }

  @Test
  public void shouldIgnoreNonPositiveContentLength() {
    analytics.sendUrlMeasurement(measurement, "akamai", 0);

    assertThat(capture.entry).doesNotContainKeys("transferSize");

    analytics.sendUrlMeasurement(measurement, "akamai", -123);

    assertThat(capture.entry).doesNotContainKeys("transferSize");
  }

  @Test
  public void shouldSendNonNullCdnName() {
    analytics.sendUrlMeasurement(measurement, "akamai", 123);

    assertThat(capture.entry).containsKey("cdn");
    assertThat(capture.entry.get("cdn")).isEqualTo("akamai");
  }

  @Test
  public void shouldIgnoreNullAndEmptyCdnName() {
    analytics.sendUrlMeasurement(measurement, null, 0);

    assertThat(capture.entry).doesNotContainKeys("cdn");

    analytics.sendUrlMeasurement(measurement, "", 0);

    assertThat(capture.entry).doesNotContainKeys("cdn");
  }
}
