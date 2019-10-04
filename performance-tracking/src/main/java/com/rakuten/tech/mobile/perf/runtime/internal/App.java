package com.rakuten.tech.mobile.perf.runtime.internal;

import com.rakuten.tech.mobile.manifestconfig.annotations.ManifestConfig;
import com.rakuten.tech.mobile.manifestconfig.annotations.MetaData;
import com.rakuten.tech.mobile.perf.BuildConfig;

@ManifestConfig
public interface App {
  @MetaData(key = "com.rakuten.tech.mobile.analytics.RATEndpoint", value = "https://rat.rakuten.co.jp/")
  String ratEndPoint();

  @MetaData(key = "com.rakuten.tech.mobile.perf.ConfigurationUrlPrefix", value = BuildConfig.DEFAULT_CONFIG_URL_PREFIX)
  String configUrlPrefix();

  @MetaData(key = "com.rakuten.tech.mobile.perf.LocationUrlPrefix", value = BuildConfig.DEFAULT_LOCATION_URL_PREFIX)
  String locationUrlPrefix();

  @MetaData(key = "com.rakuten.tech.mobile.perf.MaxMetricDuration", value = "10000")
  float maxMetricDuration();

  @MetaData(key = "com.rakuten.tech.mobile.ras.AppId")
  String appId();

  @MetaData(key = "com.rakuten.tech.mobile.ras.ProjectSubscriptionKey")
  String appKey();

  @MetaData(key = "com.rakuten.tech.mobile.relay.AppId")
  @Deprecated
  String relayAppId();

  @MetaData(key = "com.rakuten.tech.mobile.relay.SubscriptionKey")
  @Deprecated
  String relayAppKey();
}
