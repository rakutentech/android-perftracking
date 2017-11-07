package com.rakuten.tech.mobile.perf.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

class EnvironmentInfo implements Observer {

  private final static long MEGA_BYTE = 1024 * 1024;
  String device;
  String network;
  final String osname;
  String osversion;
  private String country = null;
  private String region = null;
  private float batterylevel;
  private final float devicetotalmemory;
  private final ActivityManager activityManager;

  EnvironmentInfo(Context context, CachingObservable<LocationData> locationObservable, CachingObservable<Float> batteryInfoObservable) {

    locationObservable.addObserver(this);
    batteryInfoObservable.addObserver(this);

    this.osname = "android";
    this.device = Build.MODEL;
    this.osversion = Build.VERSION.RELEASE;
    this.activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      this.devicetotalmemory = (float) ((double) (getMemoryInfo().totalMem) / (double) MEGA_BYTE);
    } else {
      this.devicetotalmemory = -1L;
    }

    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    if (tm != null) {
      this.country = tm.getSimCountryIso();
      this.network = tm.getNetworkOperatorName();
    }

    if (this.country == null || "".equals(this.country)) {
      this.country = Locale.getDefault().getCountry();
    }

    if (this.country != null) {
      this.country = this.country.toUpperCase();
    }

    if (this.network == null || "".equals(this.network)) {
      this.network = "wifi";
    }

    if (locationObservable.getCachedValue() != null) {
      this.update(locationObservable, locationObservable.getCachedValue());
    }

  }

  private ActivityManager.MemoryInfo getMemoryInfo() {
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    if (activityManager != null) {
      activityManager.getMemoryInfo(mi);
    }
    return mi;
  }

  float getAppUsedMemory() {
    return (float)((double)(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (double)MEGA_BYTE);
  }

  float getDeviceTotalMemory() {
    return this.devicetotalmemory;
  }

  synchronized float getDeviceFreeMemory() {
    return (float) ((double) (getMemoryInfo().availMem) / (double) MEGA_BYTE);
  }

  synchronized float getBatteryLevel() {
    return this.batterylevel;
  }

  synchronized String getCountry() {
    return this.country;
  }

  synchronized String getRegion() {
    return this.region;
  }

  @Override
  public void update(Observable observable, Object value) {
    if (value instanceof LocationData) {
      synchronized (this) {
        this.region = ((LocationData) value).region;
        this.country = ((LocationData) value).country;
      }
    } else {
      synchronized (this) {
        this.batterylevel = (float) value;
      }
    }
  }
}
