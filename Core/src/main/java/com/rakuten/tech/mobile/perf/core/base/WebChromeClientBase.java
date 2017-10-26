package com.rakuten.tech.mobile.perf.core.base;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.rakuten.tech.mobile.perf.core.Tracker;

public class WebChromeClientBase extends WebChromeClient {

  public void onProgressChanged(WebView view, int newProgress) {
    Tracker.prolongMetric();
    super.onProgressChanged(view, newProgress);
  }
}
