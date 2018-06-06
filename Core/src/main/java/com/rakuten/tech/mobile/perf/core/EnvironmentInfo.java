package com.rakuten.tech.mobile.perf.core;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import java.util.Observable;
import java.util.Observer;

class EnvironmentInfo implements Observer {

  private final static long MEGA_BYTE = 1024 * 1024;
  String device;
  String network;
  final String osName;
  String osVersion;
  private String country;
  private String region = null;
  private float batteryLevel;
  private final long deviceTotalMemory;
  private final ActivityManager activityManager;

  EnvironmentInfo(Context context, CachingObservable<LocationData> locationObservable, CachingObservable<Float> batteryInfoObservable) {

    locationObservable.addObserver(this);
    batteryInfoObservable.addObserver(this);

    this.osName = "android";
    this.device = Build.MODEL;
    this.osVersion = Build.VERSION.RELEASE;
    this.activityManager = (ActivityManager) context.getSystemService(Activity.ACTIVITY_SERVICE);

    // Hard code Build.VERSION_CODES.JELLY_BEAN to avoid crash on lower os version.
    if (Build.VERSION.SDK_INT >= 16) {
      this.deviceTotalMemory = Math.round((double) (getMemoryInfo(activityManager).totalMem) / (double) MEGA_BYTE);
    } else {
      this.deviceTotalMemory = -1L;
    }

    this.network = TelephonyUtil.getNetwork(context);
    this.country = TelephonyUtil.getCountryCode(context);

    if (locationObservable.getCachedValue() != null) {
      this.update(locationObservable, locationObservable.getCachedValue());
    }

  }

  private static ActivityManager.MemoryInfo getMemoryInfo(ActivityManager activityManager) {
    ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
    if (activityManager != null) {
      activityManager.getMemoryInfo(mi);
    }
    return mi;
  }

  long getAppUsedMemory() {
    return Math.round((double) (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (double) MEGA_BYTE);
  }

  long getDeviceTotalMemory() {
    return this.deviceTotalMemory;
  }

  synchronized long getDeviceFreeMemory() {
    return Math.round((double) (getMemoryInfo(activityManager).availMem) / (double) MEGA_BYTE);
  }

  synchronized float getBatteryLevel() {
    return this.batteryLevel;
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
        this.batteryLevel = (float) value;
      }
    }
  }
}
