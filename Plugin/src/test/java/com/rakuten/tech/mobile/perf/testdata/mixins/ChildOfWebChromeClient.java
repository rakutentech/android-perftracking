package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.webkit.WebView;
import com.rakuten.tech.mobile.perf.core.base.WebChromeClientBase;

// this is test data only for mixin, not rebasing, so parent class is WebChromeClientBase,
// not WebChromeClient
public class ChildOfWebChromeClient extends WebChromeClientBase {
  public void onProgressChanged(WebView view, int newProgress) {
  }
}
