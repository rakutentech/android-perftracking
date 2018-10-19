package com.rakuten.tech.mobile.perf.core;

import java.util.Map;

public interface Analytics {
  String CDN_HEADER = "X-CDN-Served-From";
  void sendEvent(String name, Map<String, ?> data);
}
