package com.rakuten.tech.mobile.perf.runtime.internal;

import static com.rakuten.tech.mobile.perf.runtime.TestCondition.keyValue;
import static org.assertj.core.api.Java6Assertions.assertThat;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.rakuten.tech.mobile.perf.BuildConfig;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestData;
import org.junit.Rule;
import org.junit.Test;

public class GeoLocationRequestSpec extends RobolectricUnitSpec {

  @Test
  public void shouldConstructWithNullableParameters() {
    GeoLocationRequest request = new GeoLocationRequest("", "", null, null);
    assertThat(request).isNotNull();
    assertThat(request.getMethod()).isEqualTo(Request.Method.GET);
  }

  @Test
  public void shouldBuildUrlWithCustomPrefix() {
    GeoLocationRequest request =
        new GeoLocationRequest("https://other.prefix.com/abc/xyz/v1", "", null, null);
    assertThat(request.getUrl()).isEqualTo("https://other.prefix.com/abc/xyz/v1");
  }

  @Test
  public void shouldSetSubscriptionKeyHeader() {
    String testKey = "testKey";
    GeoLocationRequest request = new GeoLocationRequest("", testKey, null, null);
    assertThat(request.getHeaders()).has(keyValue("Ocp-Apim-Subscription-Key", testKey));
  }

  @Rule public TestData data = new TestData("geolocation-api-response.json");

  @Test
  public void shouldParseResponse() throws VolleyError {
    GeoLocationRequest request = new GeoLocationRequest("", "", null, null);
    GeoLocationResult response = request.parseResponse(data.content);
    assertThat(response).isNotNull();
    assertThat(response.getRegionName().equals("Tokyo"));
  }

  @Test(expected = VolleyError.class)
  public void shouldNotFailOnInvalidResponseString() throws VolleyError {
    GeoLocationRequest request = new GeoLocationRequest("", "", null, null);
    GeoLocationResult result = request.parseResponse("some invalid json [[[[}}}");
    assertThat(result).isNull();
  }
}
