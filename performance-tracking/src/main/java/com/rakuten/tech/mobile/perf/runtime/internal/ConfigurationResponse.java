package com.rakuten.tech.mobile.perf.runtime.internal;

import android.support.annotation.NonNull;
import android.util.Log;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Configuration Result
 */
class ConfigurationResponse {

  private static final String TAG = ConfigurationResponse.class.getSimpleName();
  private double enablePercent;
  private boolean enableNonMetricMeasurement;
  private String sendUrl;
  private Map<String, String> header;
  private Map<String, String> modules;

  private static final String ENABLE_PERFORMANCE_TRACKING_KEY = "enablePerformanceTracking";
  private static final String ENABLE_RAT_KEY = "enableRat";

  ConfigurationResponse(@NonNull String json) {
    try {
      JSONObject obj = new JSONObject(json);
      enablePercent = obj.getDouble("enablePercent");
      enableNonMetricMeasurement = obj.getBoolean("enableNonMetricMeasurement");
      sendUrl = obj.getString("sendUrl");
      header = toStringMap(obj.getJSONObject("sendHeaders"));
      modules = toStringMap(obj.getJSONObject("modules"));
    } catch (JSONException t) {
      Log.d(TAG, "Failed to parse configuration response JSON", t);
    }
  }

  private static Map<String, String> toStringMap(@NonNull JSONObject json) throws JSONException {
    HashMap<String, String> map = new HashMap<>();
    Iterator<String> it = json.keys();
    while (it.hasNext()) {
      String key = it.next();
      map.put(key, json.getString(key));
    }
    return map;
  }

  @NonNull
  @Override
  public String toString() {
    try {
      return new JSONObject()
          .put("enablePercent", enablePercent)
          .put("enableNonMetricMeasurement", enableNonMetricMeasurement)
          .put("sendUrl", sendUrl)
          .put("sendHeaders", new JSONObject(header))
          .put("modules", new JSONObject(modules))
          .toString();
    } catch (JSONException | NullPointerException e ) {
      return "{}";
    }
  }

  double getEnablePercent() {
    return enablePercent;
  }

  boolean shouldEnableNonMetricMeasurement() {
    return enableNonMetricMeasurement;
  }

  boolean shouldSendToPerfTracking() {
    return (modules != null && modules.containsKey(ENABLE_PERFORMANCE_TRACKING_KEY))
        ? Boolean.valueOf(modules.get(ENABLE_PERFORMANCE_TRACKING_KEY))
        : true;
  }

  boolean shouldSendToAnalytics() {
    return (modules != null && modules.containsKey(ENABLE_RAT_KEY))
        ? Boolean.valueOf(modules.get(ENABLE_RAT_KEY))
        : false;
  }

  String getSendUrl() {
    return sendUrl;
  }

  Map<String, String> getHeader() {
    return header;
  }
}
