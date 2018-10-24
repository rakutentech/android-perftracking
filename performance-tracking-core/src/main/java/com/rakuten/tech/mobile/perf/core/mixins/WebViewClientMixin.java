package com.rakuten.tech.mobile.perf.core.mixins;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import com.rakuten.tech.mobile.perf.core.base.WebViewClientBase;

@MixSubclassOf(WebViewClient.class)
public class WebViewClientMixin extends WebViewClientBase {

  @ReplaceMethod
  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    Tracker.prolongMetric();
    com_rakuten_tech_mobile_perf_page_trackingId = Tracker.startUrl(url, "VIEW");
    onPageStarted(view, url, favicon);
  }

  @ReplaceMethod
  public void onPageFinished(WebView view, String url) {
    Tracker.prolongMetric();
    Tracker.endMetric();
    Tracker.endUrl(com_rakuten_tech_mobile_perf_page_trackingId, 200, null);
    com_rakuten_tech_mobile_perf_page_trackingId = 0;
    onPageFinished(view, url);
  }

  @ReplaceMethod
  public void onReceivedHttpError(WebView view, WebResourceRequest request,
      WebResourceResponse errorResponse) {
    Tracker.prolongMetric();
    Tracker.endMetric();
    Tracker.endUrl(com_rakuten_tech_mobile_perf_page_trackingId, errorResponse.getStatusCode(),
        null);
    com_rakuten_tech_mobile_perf_page_trackingId = 0;
    onReceivedHttpError(view, request, errorResponse);
  }
}

