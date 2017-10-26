package com.rakuten.tech.mobile.perf.runtime.internal;

class GeoLocationResult {

  private final String countryName;
  private final String regionName;

  GeoLocationResult(String country, String region) {
    countryName = country;
    regionName = region;
  }

  String getCountryName() {
    return countryName;
  }

  String getRegionName() {
    return regionName;
  }
}
