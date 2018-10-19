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

  private ConfigurationResult(Parcel in) {
    enablePercent = in.readDouble();
    enableNonMetricMeasurement = in.readByte() == 1;
    sendUrl = in.readString();
    Bundle bundle = in.readBundle();
    header = new HashMap<>();
    for (String key : bundle.keySet()) {
      header.put(key, bundle.getString(key));
    }
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeDouble(enablePercent);
    dest.writeByte((byte) (enableNonMetricMeasurement ? 1 : 0));
    dest.writeString(sendUrl);
    Bundle bundle = new Bundle();
    for (String key : header.keySet()) {
      bundle.putString(key, header.get(key));
    }
    dest.writeBundle(bundle);
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

  String getSendUrl() {
    return sendUrl;
  }

  Map<String, String> getHeader() {
    return header;
  }
}
