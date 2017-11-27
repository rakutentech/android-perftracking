package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Shared functions for Relay SDKs
 */
class Util {

  private static final String SUBSCRIPTION_META_KEY = "com.rakuten.tech.mobile.relay.SubscriptionKey";
  private static final String RELAY_APP_ID = "com.rakuten.tech.mobile.relay.AppId";

  /**
   * Extract the (shared) relay subscription key from the app's manifest. The key is expected as
   * shown below:
   *
   * ```xml
   * <manifest>
   *   <application>
   *     <meta-data android:name="com.rakuten.tech.mobile.relay.SubscriptionKey"
   *     android:value="subscriptionKey" />
   *   </application>
   * </manifest>
   * ```
   *
   * @param context application context
   * @return subscription key if present, null otherwise
   */
  static @Nullable String getSubscriptionKey(@NonNull Context context) {
    return getMeta(context, SUBSCRIPTION_META_KEY);
  }

  /**
   * Extract the relay app id from the app's manifest. The appId is expected as
   * shown below:
   *
   * ```xml
   * <manifest>
   *   <application>
   *     <meta-data android:name="com.rakuten.tech.mobile.relay.AppId"
   *     android:value="appId" />
   *   </application>
   * </manifest>
   * ```
   *
   * @param context application context
   * @return relay app id if present, null otherwise
   */
  static @Nullable String getRelayAppId(@NonNull Context context) {
    return getMeta(context, RELAY_APP_ID);
  }

  static @Nullable String getMeta(@NonNull Context context, @NonNull String key) {
    try {
      Bundle metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(),
          PackageManager.GET_META_DATA).metaData;
      return metaData != null ? metaData.getString(key) : null;
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }
}
