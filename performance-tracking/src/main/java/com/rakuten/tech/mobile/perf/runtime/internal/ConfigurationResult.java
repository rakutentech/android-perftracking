package com.rakuten.tech.mobile.perf.runtime.internal;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.gson.annotations.SerializedName;
import java.util.HashMap;
import java.util.Map;

/** Configuration Result */
class ConfigurationResult implements Parcelable {

  @SerializedName("enablePercent")
  private double enablePercent;

  @SerializedName("enableNonMetricMeasurement")
  private boolean enableNonMetricMeasurement;

  @SerializedName("sendUrl")
  private String sendUrl;

  @SerializedName("sendHeaders")
  private Map<String, String> header;

  @SerializedName("modules")
  private Map<String, String> modules;

  private static final String ENABLE_PERFORMANCE_TRACKING_KEY = "enablePerformanceTracking";
  private static final String ENABLE_RAT_KEY = "enableRat";

  private ConfigurationResult(Parcel in) {
    enablePercent = in.readDouble();
    enableNonMetricMeasurement = in.readByte() == 1;
    sendUrl = in.readString();

    Bundle headerBundle = in.readBundle();
    header = new HashMap<>();
    for (String key : headerBundle.keySet()) {
      header.put(key, headerBundle.getString(key));
    }

    Bundle modulesBundle = in.readBundle();
    modules = new HashMap<>();
    for (String key : modulesBundle.keySet()) {
      modules.put(key, modulesBundle.getString(key));
    }
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeDouble(enablePercent);
    dest.writeByte((byte) (enableNonMetricMeasurement ? 1 : 0));
    dest.writeString(sendUrl);

    Bundle headerBundle = new Bundle();
    for (String key : header.keySet()) {
      headerBundle.putString(key, header.get(key));
    }
    dest.writeBundle(headerBundle);

    Bundle moduleBundle = new Bundle();
    for (String key : modules.keySet()) {
      moduleBundle.putString(key, modules.get(key));
    }
    dest.writeBundle(moduleBundle);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ConfigurationResult> CREATOR =
      new Creator<ConfigurationResult>() {
        @Override
        public ConfigurationResult createFromParcel(Parcel in) {
          return new ConfigurationResult(in);
        }

        @Override
        public ConfigurationResult[] newArray(int size) {
          return new ConfigurationResult[size];
        }
      };

  double getEnablePercent() {
    return enablePercent;
  }

  boolean shouldEnableNonMetricMeasurement() {
    return enableNonMetricMeasurement;
  }

  boolean shouldSendToPerfTracking() {
    return (modules != null && modules.containsKey(ENABLE_PERFORMANCE_TRACKING_KEY))
        ? Boolean.valueOf(modules.get(ENABLE_PERFORMANCE_TRACKING_KEY))
        : true;
  }

  boolean shouldSendToAnalytics() {
    return (modules != null && modules.containsKey(ENABLE_RAT_KEY))
        ? Boolean.valueOf(modules.get(ENABLE_RAT_KEY))
        : false;
  }

  String getSendUrl() {
    return sendUrl;
  }

  Map<String, String> getHeader() {
    return header;
  }
}
