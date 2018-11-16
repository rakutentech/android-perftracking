package com.rakuten.tech.mobile.perf.runtime.internal;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import org.json.JSONException;
import org.json.JSONObject;

/** GeoLocationRequest */
class GeoLocationRequest extends BaseRequest<GeoLocationResult> {

  GeoLocationRequest(
      @NonNull String urlPrefix,
      @Nullable String subscriptionKey,
      @Nullable Response.Listener<GeoLocationResult> listener,
      @Nullable Response.ErrorListener errorListener) {
    super(listener, errorListener);
    setMethod(Method.GET);
    setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
    setUrl(urlPrefix);
  }

  @Override
  protected GeoLocationResult parseResponse(String response) {
    return new GeoLocationResult(response);
  }
}
