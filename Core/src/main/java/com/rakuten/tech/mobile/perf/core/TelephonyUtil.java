package com.rakuten.tech.mobile.perf.core;

import android.content.Context;
import android.telephony.TelephonyManager;
import java.util.Locale;

public final class TelephonyUtil {

  private TelephonyUtil() {}

  /**
   * Returns the country code from the device SIM card if present,
   * or the device's Locale country code if there is no SIM card.
   *
   * @param context application context
   * @return country code
   */
  public static String getCountryCode(Context context) {
    String country = null;

    TelephonyManager tm = telephonyManager(context);
    if (tm != null) {
      country = tm.getSimCountryIso();
    }

    if (country == null || "".equals(country)) {
      country = Locale.getDefault().getCountry();
    }

    if (country != null) {
      country = country.toUpperCase();
    }

    return country;
  }

  /**
   * Returns the network operator name for this device
   * or "wifi" if there is no network name
   *
   * @param context application context
   * @return network operator name
   */
  static String getNetwork(Context context) {
    String network = null;

    TelephonyManager tm = telephonyManager(context);
    if (tm != null) {
      network = tm.getNetworkOperatorName();
    }

    if (network == null || "".equals(network)) {
      network = "wifi";
    }

    return network;
  }

  private static TelephonyManager telephonyManager(Context context) {
    return (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
  }
}
