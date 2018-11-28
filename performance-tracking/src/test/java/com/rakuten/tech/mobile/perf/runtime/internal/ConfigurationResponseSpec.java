package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;

import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import org.junit.Rule;
import org.junit.Test;

public class ConfigurationResponseSpec extends RobolectricUnitSpec {

  @Rule public TestData data = new TestData("configuration-api-response.json");

  @Test
  public void shouldParseJsonWithoutError() {

    ConfigurationResponse response = new ConfigurationResponse(data.content);

    assertThat(response.getEnablePercent()).isEqualTo(100);
    assertThat(response.getSendUrl()).isEqualTo("https://secrect.event.host.net/measurements/messages?timeout=5&api-version=2014-01");
    assertThat(response.getHeader().keySet()).contains("Authorization", "BrokerProperties", "Content-Type");
    assertThat(response.shouldSendToAnalytics()).isTrue();
    assertThat(response.shouldSendToPerfTracking()).isTrue();
    assertThat(response.shouldEnableNonMetricMeasurement()).isTrue();
  }

  @Test
  public void shouldNotThrowOnFailedParsing() {
    ConfigurationResponse response = new ConfigurationResponse("etauh889uee");

    assertThat(response).isNotNull(); // no exception
  }

  @Test
  public void shouldSerializeToJsonString() {
    ConfigurationResponse response1 = new ConfigurationResponse(data.content);

    ConfigurationResponse response2 = new ConfigurationResponse(response1.toString());

    assertThat(response1.getSendUrl()).isEqualTo(response2.getSendUrl());
    assertThat(response1.getEnablePercent()).isEqualTo(response2.getEnablePercent());
    assertThat(response1.getHeader().keySet()).containsAll(response2.getHeader().keySet());
    assertThat(response1.shouldEnableNonMetricMeasurement()).isEqualTo(response2.shouldEnableNonMetricMeasurement());
    assertThat(response1.shouldSendToAnalytics()).isEqualTo(response2.shouldSendToAnalytics());
    assertThat(response1.shouldSendToPerfTracking()).isEqualTo(response2.shouldSendToPerfTracking());
  }
}
