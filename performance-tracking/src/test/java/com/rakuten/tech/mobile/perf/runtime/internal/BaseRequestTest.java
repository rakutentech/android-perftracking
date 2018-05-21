package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;

import android.net.Uri;
import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Test;

public class BaseRequestTest extends RobolectricUnitSpec {

  private TestRequest request;

  @Before
  public void setUp() {
    request = new TestRequest();
  }

  @Test
  public void default_expectInitialized() throws AuthFailureError {
    assertThat(request.getUrl()).isEqualTo("");
    assertThat(request.getMethod()).isEqualTo(Request.Method.GET);
    assertThat(request.getBody()).isNull();
    assertThat(request.getHeaders()).isEmpty();
  }

  @Test
  public void setMethod() {
    request.setMethod(Request.Method.PUT);
    assertThat(request.getMethod()).isEqualTo(Request.Method.PUT);
  }

  @Test
  public void setUrl() {
    request.setUrl("http://www.google.com/search?query=hans");
    assertThat(request.getUrl()).isEqualTo("http://www.google.com/search?query=hans");
  }

  @Test
  public void getUrl_withQueryParam() {
    request.setUrl("http://www.google.com/search?query=hans");
    request.setQueryParam("key", "value");
    assertThat(request.getUrl()).isEqualTo("http://www.google.com/search?query=hans&key=value");
  }

  @Test
  public void setHeader() {
    request.setHeader("name", "flo");
    request.setHeader("name", "hans");
    assertThat(request.getHeaders()).hasSize(1);
    assertThat(request.getHeaders().get("name")).isEqualTo("hans");
  }

  @Test
  public void setHeaderWithNull() {
    request.setHeader("name", "flo");
    request.setHeader("name", null);
    assertThat(request.getHeaders()).isEmpty();
  }

  @Test
  public void removeHeader() {
    request.setHeader("name", "flo");
    request.removeHeader("name");
    assertThat(request.getHeaders()).isEmpty();
  }

  @Test
  public void setQueryParam() {
    request.setQueryParam("name", "flo");
    request.setQueryParam("name", "hans");
    assertThat(getQueryParams(request).get("name")).isEqualTo("hans");
  }

  @Test
  public void setQueryParamWithNull() {
    request.setQueryParam("name", "flo");
    request.setQueryParam("name", null);
    assertThat(getQueryParams(request)).isEmpty();
  }

  @Test
  public void appendQueryParam() {
    request.appendQueryParam("name", "flo");
    request.appendQueryParam("name", "hans");
    assertThat(getQueryParams(request).get("name")).isEqualTo("flo;hans");
  }

  @Test
  public void removeQueryParam() {
    request.appendQueryParam("name", "flo");
    request.removeQueryParam("name");
    assertThat(getQueryParams(request)).isEmpty();
  }

  @Test
  public void parseNetworkResponse_successResponse()
      throws UnsupportedEncodingException, ExecutionException, InterruptedException {
    Response<String> actual = request.parseNetworkResponse(createResponse("hello world"));
    assertThat(actual.result).isEqualTo("hello world");
    assertThat(actual.error).isNull();
    assertThat(actual.cacheEntry).isNull();
    assertThat(actual.intermediate).isFalse();
  }

  @Test
  public void parseNetworkResponse_errorResponse()
      throws UnsupportedEncodingException, ExecutionException, InterruptedException {
    request.mThrowException = true;
    Response<String> actual = request.parseNetworkResponse(createResponse("hello world"));
    assertThat(actual.result).isNull();
    assertThat(actual.error).isInstanceOf(VolleyError.class);
    assertThat(actual.cacheEntry).isNull();
    assertThat(actual.intermediate).isFalse();
  }

  @Test
  public void parseNetworkResponse_withHeaderCachingInstruction()
      throws UnsupportedEncodingException, ExecutionException, InterruptedException {
    Map<String, String> headers = new HashMap<>();
    headers.put("Cache-Control", "max-age=1"); // 1sec
    NetworkResponse response = new NetworkResponse(200, "hello world".getBytes(), headers, false);
    Response<String> actual = request.parseNetworkResponse(response);
    assertThat(actual.cacheEntry).isNotNull();
    assertThat(actual.cacheEntry.data).isEqualTo("hello world".getBytes());
    assertThat(actual.cacheEntry.etag).isNull();
    assertThat(actual.cacheEntry.responseHeaders).hasSize(1);
    assertThat(actual.cacheEntry.ttl).isGreaterThan(System.currentTimeMillis());
    assertThat(actual.cacheEntry.ttl).isLessThan(System.currentTimeMillis() + 2000);
  }

  private NetworkResponse createResponse(String text) {
    return new NetworkResponse(200, text.getBytes(), Collections.<String, String>emptyMap(), false);
  }

  private class TestRequest extends BaseRequest<String> {

    private boolean mThrowException = false;

    public TestRequest() {
      super(null, null);
    }

    @Override
    protected String parseResponse(String response) throws VolleyError {
      if (mThrowException) {
        throw new VolleyError("Throwing exception");
      }
      return response;
    }
  }

  private static Map<String, String> getQueryParams(Request<?> request) {
    Uri uri = Uri.parse(request.getUrl());
    Map<String, String> result = new HashMap<>();
    for (String key : uri.getQueryParameterNames()) {
      for (String value : uri.getQueryParameters(key)) {
        if (result.containsKey(key)) {
          result.put(key, result.get(key) + ";" + value);
        } else {
          result.put(key, value);
        }
      }
    }
    return result;
  }
}
