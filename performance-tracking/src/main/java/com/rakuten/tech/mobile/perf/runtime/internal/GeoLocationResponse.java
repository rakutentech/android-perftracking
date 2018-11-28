package com.rakuten.tech.mobile.perf.runtime.internal;

import org.json.JSONException;
import org.json.JSONObject;

class GeoLocationResponse {

  private final String country;
  private final String region;

  GeoLocationResponse(String json) {
    String region;
    String country;
    try {
      JSONObject location = new JSONObject(json)
          .getJSONArray("list")
          .getJSONObject(0);

      region = location
          .getJSONArray("subdivisions")
          .getJSONObject(0)
          .getJSONObject("names")
          .getString("en");

      country = location
          .getJSONObject("country")
          .getString("iso_code");
    } catch (JSONException ignored) {
      country = "";
      region = "";
    }
    this.region = region;
    this.country = country;
  }

  String getCountry() {
    return country;
  }

  String getRegion() {
    return region;
  }
}
