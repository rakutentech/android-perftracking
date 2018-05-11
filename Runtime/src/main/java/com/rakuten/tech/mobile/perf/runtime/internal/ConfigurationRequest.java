package com.rakuten.tech.mobile.perf.runtime.internal;

import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import com.android.volley.Response;
import com.google.gson.Gson;
import com.rakuten.tech.mobile.perf.BuildConfig;

/**
 * ConfigurationRequest
 */

class ConfigurationRequest extends BaseRequest<ConfigurationResult> {

  private static final String DEFAULT_URL_PREFIX = BuildConfig.DEFAULT_CONFIG_URL_PREFIX;

  ConfigurationRequest(@Nullable String urlPrefix, String subscriptionKey, ConfigurationParam param,
      @Nullable Response.Listener<ConfigurationResult> listener,
      @Nullable Response.ErrorListener errorListener) {
    super(listener, errorListener);
    setMethod(Method.GET);
    setHeader("Ocp-Apim-Subscription-Key", subscriptionKey);
    String prefix = urlPrefix != null ? urlPrefix : DEFAULT_URL_PREFIX;
    Uri uri = Uri.parse(prefix);
    uri = uri.buildUpon()
        .appendPath("platform").appendPath(param.platform)
        .appendPath("app").appendPath(param.appId)
        .appendPath("version").appendPath(param.appVersion)
        .appendPath("") // for trailing slash
        .build();
    setUrl(uri.toString());

    setQueryParam("sdk", param.sdkVersion);
    setQueryParam("country", param.countryCode);
    setQueryParam("osVersion", param.osVersion);
    setQueryParam("device", param.device);
  }

  @Override
  @Nullable
  protected ConfigurationResult parseResponse(String response) {
    ConfigurationResult result = null;
    try {
      result = new Gson().fromJson(response, ConfigurationResult.class);
    } catch (Exception e) {
      Log.e(ConfigurationRequest.class.getSimpleName(), e.getMessage());
    }
    return result;
  }
}
