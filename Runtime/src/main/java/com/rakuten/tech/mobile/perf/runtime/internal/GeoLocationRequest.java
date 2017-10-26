package com.rakuten.tech.mobile.perf.runtime.internal;

import android.support.annotation.Nullable;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.rakuten.tech.mobile.perf.BuildConfig;

/**
 * GeoLocationRequest
 */

class GeoLocationRequest extends BaseRequest<GeoLocationResult> {

  private static final String DEFAULT_URL_PREFIX = BuildConfig.DEFAULT_LOCATION_URL_PREFIX;

  GeoLocationRequest(@Nullable String urlPrefix, String subscriptionKey,
      @Nullable Response.Listener<GeoLocationResult> listener,
      @Nullable Response.ErrorListener errorListener) {
    super(listener, errorListener);
    setMethod(Method.GET);
    setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
    setUrl(urlPrefix != null ? urlPrefix : DEFAULT_URL_PREFIX);
  }

  @Override
  protected GeoLocationResult parseResponse(String response) throws VolleyError {
    try {
      String subdivisionNamesEn = new JsonParser().parse(response).getAsJsonObject()
          .get("list").getAsJsonArray()
          .get(0).getAsJsonObject()
          .get("subdivisions").getAsJsonArray()
          .get(0).getAsJsonObject()
          .get("names").getAsJsonObject()
          .get("en").getAsString();

      String countryIsoCode = new JsonParser().parse(response).getAsJsonObject()
          .get("list").getAsJsonArray()
          .get(0).getAsJsonObject()
          .get("country").getAsJsonObject()
          .get("iso_code").getAsString();

      return new GeoLocationResult(countryIsoCode, subdivisionNamesEn);
    } catch (JsonSyntaxException e) {
      throw new VolleyError(e.getMessage(), e.getCause());
    }
  }
}
