package com.rakuten.tech.mobile.perf.core.mixins;

import android.webkit.WebChromeClient;
import android.webkit.WebView;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import com.rakuten.tech.mobile.perf.core.base.WebChromeClientBase;

@MixSubclassOf(WebChromeClient.class)
public class WebChromeClientMixin extends WebChromeClientBase {

  @ReplaceMethod
  public void onProgressChanged(WebView view, int newProgress) {
    Tracker.prolongMetric();
    onProgressChanged(view, newProgress);
  }
}
