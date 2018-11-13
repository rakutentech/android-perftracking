package com.rakuten.tech.mobile.perf.core;

import java.util.Map;

/**
 * Public configuration struct
 */
@SuppressWarnings("WeakerAccess")
public class Config {

  public String app;
  public String version;
  public String eventHubUrl;
  public Map<String, String> header;
  public boolean debug;
  public String relayAppId;
  public boolean enableNonMetricMeasurement;
  public boolean enablePerfTrackingEvents;
  public boolean enableAnalyticsEvents;
}
