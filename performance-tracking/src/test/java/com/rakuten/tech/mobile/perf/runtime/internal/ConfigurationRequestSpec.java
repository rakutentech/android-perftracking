package com.rakuten.tech.mobile.perf.runtime.internal;

import static com.rakuten.tech.mobile.perf.runtime.TestCondition.keyValue;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.android.volley.Request;
import com.rakuten.tech.mobile.perf.BuildConfig;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ConfigurationRequestSpec extends RobolectricUnitSpec {

  private ConfigurationParam.Builder builder;

  @Before
  public void initConfig() {
    builder =
        new ConfigurationParam.Builder()
            .setAppId("testId")
            .setAppVersion("testVersion")
            .setSdkVersion("testSdkVersion")
            .setCountryCode("testCountryCode")
            .setPlatform("testPlatform")
            .setOsVersion("testOsVersion")
            .setDevice("testDevice");
  }

  @Test
  public void shouldConstructWithNullableParameters() {
    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request).isNotNull();
    assertThat(request.getMethod()).isEqualTo(Request.Method.GET);
  }

  @Test
  public void shouldBuildUrlWithDefaultUrlPrefix() {
    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).startsWith(BuildConfig.DEFAULT_CONFIG_URL_PREFIX);
  }

  @Test
  public void shouldBuildUrlWithCustomDomain() {
    ConfigurationRequest request =
        new ConfigurationRequest("https://rakuten.co.jp", "", builder.build(), null, null);
    assertThat(request.getUrl()).startsWith("https://rakuten.co.jp/");
  }

  @Test
  public void shouldBuildUrlWithCustomPrefix() {
    ConfigurationRequest request =
        new ConfigurationRequest(
            "https://other.prefix.com/abc/xyz/v1", "", builder.build(), null, null);
    assertThat(request.getUrl()).startsWith("https://other.prefix.com/abc/xyz/v1/");
  }

  @Test
  public void shouldBuildUrlWithPlatform() {
    builder.setPlatform("testPlatform");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("/platform/testPlatform/");
  }

  @Test
  public void shouldBuildUrlWithAppId() {
    builder.setAppId("testId");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("/app/testId/");
  }

  @Test
  public void shouldBuildUrlWithAppVersion() {
    builder.setAppVersion("testVersion");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("/version/testVersion/");
  }

  @Test
  public void shouldBuildUrlWithSdkVersion() {
    builder.setSdkVersion("testSdkVersion");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("sdk=testSdkVersion");
  }

  @Test
  public void shouldBuildUrlWithCountryCode() {
    builder.setCountryCode("testCountryCode");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("country=testCountryCode");
  }

  @Test
  public void shouldBuildUrlWithOsVersion() {
    builder.setOsVersion("testOsVersion");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("osVersion=testOsVersion");
  }

  @Test
  public void shouldBuildUrlWithDevice() {
    builder.setDevice("test device name");

    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    assertThat(request.getUrl()).contains("device=test%20device%20name");
  }

  @Test
  public void shouldSetSubscriptionKeyHeader() {
    String testKey = "testKey";
    ConfigurationRequest request =
        new ConfigurationRequest(null, testKey, builder.build(), null, null);
    assertThat(request.getHeaders()).has(keyValue("Ocp-Apim-Subscription-Key", testKey));
  }

  @Rule public TestData data = new TestData("configuration-api-response.json");

  @Test
  public void shouldParseResponse() {
    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    ConfigurationResult response = request.parseResponse(data.content);
    assertThat(response).isNotNull();
    assertThat(response.getSendUrl())
        .isEqualTo(
            "https://secrect.event.host.net/measurements/messages?timeout=5&api-version=2014-01");
    assertThat(response.getEnablePercent()).isEqualTo(100.0);
    assertThat(response.getHeader()).isNotNull();
    assertThat(response.getHeader())
        .has(
            keyValue(
                "Authorization",
                "SharedAccessSignature sr=AAAA&sig=YNu53ea4ueo2u324Fdy414mRM3F1s&se=151333345&skn=SendOnly"));
    assertThat(response.getHeader())
        .has(
            keyValue(
                "BrokerProperties",
                "{\"PartitionKey\": \"com.rakuten.tech.mobile.perf.example/1.0.0\"}"));
    assertThat(response.getHeader())
        .has(keyValue("Content-Type", "application/atom+xml;type=entry;charset=utf-8"));
  }

  @Test
  public void shouldNotFailOnInvalidResponseString() {
    ConfigurationRequest request = new ConfigurationRequest(null, "", builder.build(), null, null);
    ConfigurationResult result = request.parseResponse("some invalid json [[[[}}}");
    assertThat(result).isNull();
    // no exception
  }
}
