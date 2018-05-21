package com.rakuten.tech.mobile.perf.core.detours;

import com.rakuten.tech.mobile.perf.core.annotations.DetourCall;
import com.rakuten.tech.mobile.perf.core.wrappers.HttpURLConnectionWrapper;
import com.rakuten.tech.mobile.perf.core.wrappers.HttpsURLConnectionWrapper;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import javax.net.ssl.HttpsURLConnection;

public class URLDetours {

  @DetourCall
  public static URLConnection openConnection(URL url) throws IOException {
    URLConnection conn = url.openConnection();
    if (conn instanceof HttpsURLConnection) {
      return new HttpsURLConnectionWrapper((HttpsURLConnection) conn);
    }
    if (conn instanceof HttpURLConnection) {
      return new HttpURLConnectionWrapper((HttpURLConnection) conn);
    }
    return conn;
  }
}
