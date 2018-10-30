package com.rakuten.tech.mobile.perf.core.mixins;

import com.rakuten.tech.mobile.perf.core.Analytics;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.Exists;
import com.rakuten.tech.mobile.perf.core.annotations.MixClass;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import java.io.IOException;

@Exists(com.squareup.okhttp.Call.class)
@MixClass(com.squareup.okhttp.Call.class)
public class OkHttp2CallMixin {
  Request originalRequest;

  @ReplaceMethod
  public Response execute() throws IOException {
    int id = Tracker.startUrl(originalRequest.urlString(), originalRequest.method());

    Response response = execute();

    Tracker.endUrl(id, response.code(), response.header(Analytics.CDN_HEADER), response.body().contentLength());

    return response;
  }
}
