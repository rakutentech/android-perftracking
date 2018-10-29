package com.rakuten.tech.mobile.perf.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class Analytics {
  public static final String CDN_HEADER = "X-CDN-Served-From";

  @SuppressWarnings("WeakerAccess")
  public abstract void sendEvent(String name, Map<String, ?> data);

  void sendUrlMeasurement(Measurement m, String cdnHeader, long contentLength) {
    Map<String, Object> event =  new HashMap<>();

    Map<String, Object> entry =  new HashMap<>();
    entry.put("name", m.a);
    entry.put("startTime", m.startTime);
    entry.put("endTime", m.endTime);
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
