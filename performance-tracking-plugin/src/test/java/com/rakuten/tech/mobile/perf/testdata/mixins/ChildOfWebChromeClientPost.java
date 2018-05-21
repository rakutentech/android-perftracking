package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.webkit.WebView;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.base.WebChromeClientBase;

public class ChildOfWebChromeClientPost extends WebChromeClientBase {

  public void onProgressChanged(WebView view, int newProgress) {
    Tracker.prolongMetric();
    this.com_rakuten_tech_mobile_perf_onProgressChanged(view, newProgress);
  }

  private void com_rakuten_tech_mobile_perf_onProgressChanged(WebView view, int newProgress) {
  }
}
