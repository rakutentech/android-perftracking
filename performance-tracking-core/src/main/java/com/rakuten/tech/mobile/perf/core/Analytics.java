package com.rakuten.tech.mobile.perf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Analytics {
  public static final String CDN_HEADER = "X-CDN-Served-From";

  @SuppressWarnings("WeakerAccess")
  public abstract void sendEvent(String name, Map<String, ?> data);

  /**
   * Subclasses can hook into this method to filter url measurements without knowing internals.
   *
   * @param url string url
   * @return true if url blacklisted (events will be skipped), false otherwise
   */
  protected boolean isUrlBlacklisted(String url) {
    return false;
  };

  void sendUrlMeasurement(Measurement m, String cdnHeader, long contentLength) {
    if(isUrlBlacklisted((String) m.a)) {
      return;
    }
    Map<String, Object> event =  new HashMap<>();

    Map<String, Object> entry =  new HashMap<>();
    entry.put("name", m.a);
    entry.put("startTime", m.startTime);
    entry.put("responseEnd", m.endTime);
    entry.put("duration", m.endTime - m.startTime);
    if(cdnHeader != null && cdnHeader.length() > 0) {
      entry.put("cdn", cdnHeader);
    }
    if (contentLength > 0) {
      entry.put("transferSize", contentLength);
    }

    ArrayList<Map> entries = new ArrayList<>(1);
    entries.add(entry);

    Map<String, Object> data =  new HashMap<>();
    data.put("type", "resource");
    data.put("entries", entries);

    event.put("perfdata", data);

    sendEvent("perf", event);
  }
}
