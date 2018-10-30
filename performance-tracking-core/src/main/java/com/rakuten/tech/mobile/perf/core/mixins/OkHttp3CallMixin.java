package com.rakuten.tech.mobile.perf.core.mixins;

import com.rakuten.tech.mobile.perf.core.Analytics;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.Exists;
import com.rakuten.tech.mobile.perf.core.annotations.MixImplementationOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import java.io.IOException;

@Exists(okhttp3.Call.class)
@MixImplementationOf(okhttp3.Call.class)
public class OkHttp3CallMixin {

  @ReplaceMethod
  public okhttp3.Response execute() throws IOException {
    int id = Tracker.startUrl(request().url().toString(), request().method());

    okhttp3.Response response = execute();

    Tracker.endUrl(id, response.code(), response.header(Analytics.CDN_HEADER), response.body().contentLength());

    return response;
  }

  okhttp3.Request request() {
    return null;
  }

}
