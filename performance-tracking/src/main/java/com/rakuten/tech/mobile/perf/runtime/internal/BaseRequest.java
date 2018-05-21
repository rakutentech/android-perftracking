package com.rakuten.tech.mobile.perf.runtime.internal;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.google.gson.JsonSyntaxException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.json.JSONException;

/**
 * Base Request that only supports GET Requests
 * @param <T> Response Type
 */
abstract class BaseRequest<T> extends Request<T> {

  /** tag for logging **/
  private final String TAG = getClass().getSimpleName();
  /** listener for successful responses **/
  private final @Nullable Response.Listener<T> listener;
  /** http method **/
  private int method;
  /** url (without query parameters) **/
  private Uri url = Uri.EMPTY;
  /** map of http header parameters **/
  private Map<String, String> headers = new HashMap<>();
  /** list of url query parameters **/
  private List<Param> queryParams = new ArrayList<>();

  /**
   * Key-Value Parameter
   */
  private static class Param {

    /** Key **/
    private final String key;
    /** Value **/
    private final String value;

    private Param(String key, String value) {
      this.key = key;
      this.value = value;
    }
  }

  /** Creates new request object for GET with empty URL
   * @param listener listener for successful responses
   * @param errorListener error listener in case an error happened
   */
  BaseRequest(@Nullable Response.Listener<T> listener,
      @Nullable Response.ErrorListener errorListener) {
    this(Method.GET, "", listener, errorListener);
  }

  /** Creates new request object
   * @param method http method
   * @param url http url
   * @param listener listener for successful responses
   * @param errorListener error listener in case an error happened
   */
  private BaseRequest(int method, String url, @Nullable Response.Listener<T> listener,
      @Nullable Response.ErrorListener errorListener) {
    super(method, url, errorListener);
    this.listener = listener;
  }


  /**
   * Set the method for this request.  Can be one of the values in {@link Method}.
   * @param method one of the values in {@link Method}.
   */
  void setMethod(int method) {
    this.method = method;
  }

  @Override
  public int getMethod() {
    return method;
  }

  /**
   * Set URL endpoint. This overrides both domain and path
   * @param url url endpoint. Example: https://api.some.domain.com/v2/some/path/
   */
  void setUrl(String url) {
    this.url = Uri.parse(url);
  }

  /** returns full URL (including query parameters) **/
  @Override
  public String getUrl() {
    return getOriginUrl();
  }

  private String getOriginUrl() {
    // append all parameters directly to URL
    Uri.Builder builder = url.buildUpon();
    for (Param p : queryParams) {
      builder.appendQueryParameter(p.key, p.value);
    }
    return builder.build().toString();
  }

  /**
   * Set URL query parameter. This will remove all existing parameters with the same name
   * @param name parameter name
   * @param value parameter value. If value is null, no parameter will be set
   */
  void setQueryParam(String name, @Nullable Object value) {
    removeQueryParam(name);
    if (value != null) {
      appendQueryParam(name, String.valueOf(value));
    }
  }

  /**
   * Append new URL query parameter. This will not replace existing parameters with the same name.
   * @param name parameter name
   * @param value parameter value
   */
  void appendQueryParam(String name, Object value) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }
    if (value == null) {
      throw new IllegalArgumentException("Value cannot be null");
    }
    queryParams.add(new Param(name, String.valueOf(value)));
  }

  /**
   * Remove URL query parameter. This will remove all parameters with the given name
   * @param name parameter name
   */
  void removeQueryParam(String name) {
    if (name == null) {
      throw new IllegalArgumentException("Name cannot be null");
    }
    Iterator<Param> iter = queryParams.iterator();
    while (iter.hasNext()) {
      Param p = iter.next();
      if (p.key.equals(name)) {
        iter.remove();
      }
    }
  }

  /**
   * Set http header parameter
   * @param name header parameter name
   * @param value header parameter value or null to remove header parameter
   */
  void setHeader(@NonNull String name, @Nullable String value) {
    if (value == null) {
      removeHeader(name);
    } else {
      headers.put(name, value);
    }
  }

  /**
   * Remove http header parameter
   * @param name name of parameter
   */
  void removeHeader(@NonNull String name) {
    headers.remove(name);
  }

  /**
   * get map of http header values
   * @return map with header values
   */
  @Override public Map<String, String> getHeaders() {
    return Collections.unmodifiableMap(headers);
  }

  /**
   * Volley Request method override to catch raw network Response
   *
   * @param response the raw network response
   * @return Response object with the parsed POJO
   */
  @Override protected final Response<T> parseNetworkResponse(NetworkResponse response) {
    try {
      T data = parseResponse(response);
      return Response.success(data, parseCache(response));
    } catch (JsonSyntaxException e) {
      Log.e(TAG, "Json Exception", e);
      return Response.error(new ParseError(e));
    } catch (JSONException e) {
      Log.e(TAG, "Json Exception", e);
      return Response.error(new ParseError(e));
    } catch (UnsupportedEncodingException e) {
      Log.e(TAG, "Encoding Exception", e);
      return Response.error(new ParseError(e));
    } catch (VolleyError e) {
      Log.e(TAG, e.toString());
      return Response.error(e);
    } catch (Exception e) {
      Log.e(TAG, "Uncaught exception: " + e.toString());
      return Response.error(new VolleyError(e));
    } catch (OutOfMemoryError e) {
      Log.e(TAG, e.toString());
      return Response.error(new VolleyError(e));
    }
  }

  /**
   * Parse the response into a data model or throw an exception in case of an error.
   * Returning a value will trigger {@link #deliverResponse(Object)} while throwing an exception
   * will trigger {@link #deliverError(VolleyError)}.
   * @param response network response
   * @return parsed data response
   * @throws Exception any kind of error occurred, e.g. invalid data, data format, etc.
   */
  private T parseResponse(NetworkResponse response) throws Exception {
    String responseString = new String(response.data, getResponseCharset(response).name());
    return parseResponse(responseString);
  }

  /**
   * Parse the response into a data model or throw an exception in case of an error.
   * Returning a value will trigger {@link #deliverResponse(Object)} while throwing an exception
   * will trigger {@link #deliverError(VolleyError)}.
   * This method is being called from {@link #parseResponse(NetworkResponse)}.
   *
   * @param response network response
   * @return parsed data response
   * @throws Exception any kind of error occurred, e.g. invalid data, data format, etc.
   */
  protected abstract T parseResponse(String response) throws Exception;

  /**
   * Returns the charset specified in the Content-Type of this header, or falls back to
   * UTF-8 if none can be found.
   * @param response network response
   * @return charset from header
   */
  private Charset getResponseCharset(NetworkResponse response) {
    String charset = HttpHeaderParser.parseCharset(response.headers);
    try {
      return Charset.forName(charset);
    } catch (UnsupportedCharsetException e) {
      return Charset.forName("UTF-8");
    }
  }

  /**
   * Returns caching instruction based on HTTP header or manual configuration
   * @param response the raw network response
   * @return caching instruction or null to ignore cache
   */
  private Cache.Entry parseCache(NetworkResponse response) {
    if (response.headers.get("Expires") != null
        || response.headers.get("Cache-Control") != null
        || response.headers.get("ETag") != null) {
      return HttpHeaderParser.parseCacheHeaders(response);
    } else {
      return null;
    }
  }

  /**
   * Convenience function to add this request to the queue
   * @param queue queue to add request to
   * @return this
   */
  BaseRequest<T> queue(RequestQueue queue) {
    queue.add(this);
    return this;
  }

  @Override public void deliverResponse(T response) {
    // notify observer object, thereby notifying front-end UI
    if (listener != null) {
      listener.onResponse(response);
    }
  }
}
