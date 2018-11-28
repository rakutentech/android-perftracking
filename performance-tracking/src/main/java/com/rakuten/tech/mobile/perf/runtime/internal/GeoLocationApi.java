package com.rakuten.tech.mobile.perf.runtime.internal;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface GeoLocationApi {
  @GET("/")
  Call<GeoLocationResponse> location(@Header("Ocp-Apim-Subscription-Key") String subscriptionKey);
}
