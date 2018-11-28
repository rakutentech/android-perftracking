package com.rakuten.tech.mobile.perf.runtime.internal;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

interface ConfigApi {
  @GET("platform/android/app/{app}/version/{version}")
  Call<ConfigurationResponse> config(
      @Path("app") String appId,
      @Path("version") String appVersion,
      @Header("Ocp-Apim-Subscription-Key") String subscriptionKey,
      @Query("sdk") String sdkVersion,
      @Query("country") String country,
      @Query("osVersion") String osVersion,
      @Query("device") String device
  );
}
