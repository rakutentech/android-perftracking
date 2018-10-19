package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.base.WebViewClientBase;

public class ChildOfWebViewClientPost extends WebViewClientBase {

  public void onPageStarted(WebView view, String url, Bitmap favicon) {
    Tracker.prolongMetric();
    com_rakuten_tech_mobile_perf_page_trackingId = Tracker.startUrl(url, "VIEW");
    this.com_rakuten_tech_mobile_perf_onPageStarted(view, url, favicon);
  }

  public void onPageFinished(WebView view, String url) {
    Tracker.prolongMetric();
    Tracker.endMetric();
    Tracker.endUrl(com_rakuten_tech_mobile_perf_page_trackingId, 200, url);
    com_rakuten_tech_mobile_perf_page_trackingId = 0;
    this.com_rakuten_tech_mobile_perf_onPageFinished(view, url);
  }

  public void onReceivedHttpError(WebView view, WebResourceRequest request,
      WebResourceResponse errorResponse) {
    Tracker.prolongMetric();
    Tracker.endMetric();
    Tracker.endUrl(com_rakuten_tech_mobile_perf_page_trackingId, errorResponse.getStatusCode(),
        null);
    com_rakuten_tech_mobile_perf_page_trackingId = 0;
    this.com_rakuten_tech_mobile_perf_onReceivedHttpError(view, request, errorResponse);
  }

  private void com_rakuten_tech_mobile_perf_onPageStarted(WebView view, String url, Bitmap favicon) {

  }

  private void com_rakuten_tech_mobile_perf_onPageFinished(WebView view, String url) {

  }

  private void com_rakuten_tech_mobile_perf_onReceivedHttpError(WebView view, WebResourceRequest request,
      WebResourceResponse errorResponse) {

  }
}

