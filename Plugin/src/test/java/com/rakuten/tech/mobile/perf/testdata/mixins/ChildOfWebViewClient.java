package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.graphics.Bitmap;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import com.rakuten.tech.mobile.perf.core.base.WebViewClientBase;

// this is test data only for mixin, not rebasing, so parent class is WebViewClientBase, not
// WebViewClient
public class ChildOfWebViewClient extends WebViewClientBase {

  public void onPageStarted(WebView view, String url, Bitmap favicon) {

  }

  public void onPageFinished(WebView view, String url) {

  }

  public void onReceivedHttpError(WebView view, WebResourceRequest request,
      WebResourceResponse errorResponse) {

  }
}

