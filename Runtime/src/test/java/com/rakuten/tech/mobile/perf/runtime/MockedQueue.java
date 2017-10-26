package com.rakuten.tech.mobile.perf.runtime;

import android.net.Uri;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("unused")
public class MockedQueue extends RequestQueue {

  /** all queued requests **/
  private List<Request<?>> requests = new ArrayList<>();
  /** all configured rules **/
  private List<Rule> rules = new ArrayList<>();

  private final Rule defaultRule;

  /** rule of a response **/
  private static class Rule implements RequestFilter {

    private final RuleConfiguration config;
    private boolean applied = false;

    private Rule(RuleConfiguration config) {
      this.config = config;
    }

    @Override public boolean apply(Request<?> request) {
      if (applied && !config.mRetain) {
        return false;
      }
      // check if filter match
      for (RequestFilter filter : config.requestFilters) {
        if (!filter.apply(request)) {
          return false;
        }
      }
      return true;
    }

    /**
     * Deliver configured response of rule to given request object
     * @param request request instance that receives the configured response of {@code this}
     */
    void deliverResponse(Request<?> request) {
      Object parsedResponse = null;
      try {
        if (config.rawResponse != null) {
          // BasicNetwork : http status 200-299 -> returns NetworkResponse, handled via parseNetworkResponse
          //                other status code   -> returns ServerError, handled via parseNetworkError
          if (config.rawResponse.statusCode >= 200 && config.rawResponse.statusCode <= 299) {
            // parse via parseNetworkResponse
            Method method = Request.class
                .getDeclaredMethod("parseNetworkResponse", NetworkResponse.class);
            method.setAccessible(true);
            Response response = (Response) method.invoke(request, config.rawResponse);
            if (response.error == null) {
              // deliver success response
              parsedResponse = response.result;
              Method method2 = Request.class.getDeclaredMethod("deliverResponse", Object.class);
              method2.setAccessible(true);
              method2.invoke(request, parsedResponse);
            } else {
              // deliver error response
              request.deliverError(response.error);
            }
          } else {
            VolleyError error = new ServerError(config.rawResponse);
            // parse via parseNetworkResponse
            Method method = Request.class.getDeclaredMethod("parseNetworkError", VolleyError.class);
            method.setAccessible(true);
            error = (VolleyError) method.invoke(request, error);
            // deliver error response
            request.deliverError(error);
          }
        } else if (config.errorResponse != null) {
          // deliver parsed error
          request.deliverError(config.errorResponse);
        } else {
          // deliver parsed response (which can be null)
          parsedResponse = config.parsedResponse;
          Method method = Request.class.getDeclaredMethod("deliverResponse", Object.class);
          method.setAccessible(true);
          method.invoke(request, parsedResponse);
        }
      } catch (InvocationTargetException e) {
        String name = parsedResponse != null ? parsedResponse.getClass().getName() : "null";
        throw new RuntimeException(
            "Invalid response type. Request '" + request.getUrl() + "' (" + request.getClass()
                .getName() + ") did not accept response '" + parsedResponse + "' (" + name + ")");
      } catch (Exception e) {
        throw new RuntimeException(e);
      }
      // skip rule in the future
      if (!config.mRetain) {
        applied = true;
      }
    }

    /**
     * Verify if rule has ever been used to service a network request.
     * @throws AssertionError if rule has never service a network request and is not-optional.
     */
    void verify() throws AssertionError {
      if (!applied && !config.mIsOptional) {
        throw new AssertionError("Rule " + this
            + " has never been serviced a network request. Check your test or mark this rule as optional.");
      }
    }
  }

  /**
   * Configuration to mock requests
   */
  @SuppressWarnings("WeakerAccess")
  public static class RuleConfiguration {

    private final MockedQueue mQueue;
    private List<RequestFilter> requestFilters = new ArrayList<>();
    private boolean mRetain = false;
    private boolean mIsOptional = false;
    private Object parsedResponse = null;
    private NetworkResponse rawResponse = null;
    private VolleyError errorResponse = null;

    private RuleConfiguration(MockedQueue queue) {
      mQueue = queue;
    }

    /**
     * Add a filter to accept only certain requests
     *
     * @param filter request filter
     * @return configuration builder
     */
    public RuleConfiguration whenRequest(RequestFilter filter) {
      requestFilters.add(filter);
      return this;
    }

    /**
     * Add a filter to accept only requests of type {@code clazz}
     *
     * @param clazz request type class
     * @return filter builder
     */
    public RuleConfiguration whenClass(final Class<? extends Request<?>> clazz) {
      return whenRequest(new RequestFilter() {
        @Override
        public boolean apply(Request<?> request) {
          return clazz.isInstance(request);
        }
      });
    }

    /**
     * Add a filter to only accept requests with url (fully) matches {@code url}.
     * Only the scheme, domain and path is matched, query parameters are ignored.
     *
     * @param url full url (without query parameters) to match
     * @return configuration builder
     */
    public RuleConfiguration whenUrl(final String url) {
      return whenRequest(new RequestFilter() {
        @Override
        public boolean apply(Request<?> request) {
          return Uri.parse(request.getUrl()).buildUpon()
              .clearQuery()
              .toString()
              .equals(url);
        }
      });

    }

    /**
     * Add a filter to only accept requests with url partially matching {@code path}.
     * Only the scheme, domain and path is matched, query parameters are ignored.
     *
     * @param path part of the url to match against
     * @return configuration builder
     */
    public RuleConfiguration whenPath(final String path) {
      return whenRequest(new RequestFilter() {
        @Override
        public boolean apply(Request<?> request) {
          return Uri.parse(request.getUrl()).buildUpon()
              .clearQuery()
              .toString()
              .contains(path);
        }
      });
    }

    /**
     * Add a filter to only accept requests with tag matching {@code tag}.
     *
     * @param tag tag to be matched against {@link Request#getTag()}
     * @return configuration builder
     */
    public RuleConfiguration whenTag(final String tag) {
      return whenRequest(new RequestFilter() {
        @Override
        public boolean apply(Request<?> request) {
          return tag.equals(request.getTag());
        }
      });
    }

    /**
     * If set, then this configuration will be applied to all request. (default behaviour is
     * that the configuration is only applied to the first matching request and then discard)
     * @return configuration builder
     */
    public RuleConfiguration always() {
      mRetain = true;
      return this;
    }

    /**
     * Mark this rule as optional. Optional rules will be excluded during verification using
     * {@link #verify()}. By default all rules are mandatory and will be check during verification.
     * @return configuration builder
     */
    public RuleConfiguration optional() {
      mIsOptional = true;
      return this;
    }

    /**
     * Set parsed & successful response to be delivered to requests that match this configuration.
     * @param parsedResponse parsed response to be delivered
     */
    public void returnResponse(Object parsedResponse) {
      this.parsedResponse = parsedResponse;
      mQueue.addRule(new Rule(this));
    }

    /**
     * Set error to be delivered to requests that match this configuration.
     * @param error error to be delivered
     */
    public void returnError(VolleyError error) {
      errorResponse = error;
      mQueue.addRule(new Rule(this));
    }

    /**
     * Set raw network response to be delivered to requests that match this configuration.
     * @param response raw network response to be delivered
     */
    public void returnNetworkResponse(NetworkResponse response) {
      rawResponse = response;
      mQueue.addRule(new Rule(this));
    }

    /**
     * Set raw network response to be delivered to requests that match this configuration.
     * @param httpCode http code
     * @param rawResponse raw network response as string
     */
    public void returnNetworkResponse(int httpCode, String rawResponse) {
      try {
        returnNetworkResponse(new NetworkResponse(httpCode, rawResponse.getBytes("UTF-8"),
            Collections.<String, String>emptyMap(), false));
      } catch (UnsupportedEncodingException ex) {
        throw new AssertionError(ex);
      }
    }
  }

  public MockedQueue() {
    super(null, null, 0, null);

    // define default rule
    RuleConfiguration config = new RuleConfiguration(this);
    config.errorResponse = new TimeoutError();
    defaultRule = new Rule(config);
  }

  @Override
  public void start() {
  }

  @Override
  public void stop() {
  }

  @Override
  public int getSequenceNumber() {
    return super.getSequenceNumber();
  }

  @Override
  public void cancelAll(RequestFilter filter) {
  }

  @Override
  public void cancelAll(Object tag) {
  }

  @Override
  public synchronized <T> Request<T> add(Request<T> request) {
    requests.add(request);
    processRequest(request);
    return request;
  }

  /**
   * Define a new rule to return predefined responses to queued requests
   * @return new rule configuration: Use {@code whenXxx()} to specify what requests should be matched and
   *         use {@code returnXxx()} to then specify what response should be delivered to those requests
   *         By default a rule will only be applied once, use {@code always()} to apply it more than once.
   */
  public RuleConfiguration rule() {
    return new RuleConfiguration(this);
  }

  /**
   * Add a configured mocked response
   * @param response mocked response to be delivered
   */
  private synchronized void addRule(Rule response) {
    rules.add(response);
  }

  /**
   * Clear all configured mocking rules
   */
  public synchronized void reset() {
    rules.clear();
  }

  /**
   * Checks if all configured rules have been used to service a queued request. If any rule
   * has never matched any network request and is not marked as optional, then this method will
   * an AssertionError().
   * @throws AssertionError if a non-optional rule has never serviced a network request
   */
  public synchronized void verify() throws AssertionError {
    for (Rule r : rules) {
      r.verify();
    }
  }

  /**
   * Checks if there is a appropriate response set up for the request. If found, the response will be pushed to the request and the
   * response will be removed from {@link #rules}. Otherwise, a {@link TimeoutError} will be delivered as response.
   */
  private synchronized void processRequest(Request request) {
    // look for a suitable response
    for (Rule rule : rules) {
      if (rule.apply(request)) {
        rule.deliverResponse(request);
        return;
      }
    }
    // if we got no response -> deliver timeout error
    defaultRule.deliverResponse(request);
  }

  /**
   * get list of all requests that were queued
   * @return list of queued requests
   */
  public synchronized List<Request<?>> getServicedRequests() {
    return requests;
  }

  /**
   * get list of all requests that were queued and match the filter
   * @param filter request filter
   * @return filtered list of queued requests
   */
  public synchronized List<Request<?>> getServicedRequests(RequestFilter filter) {
    List<Request<?>> requests = new ArrayList<>();
    for (Request<?> request : this.requests) {
      if (filter.apply(request)) {
        requests.add(request);
      }
    }
    return requests;
  }
}
