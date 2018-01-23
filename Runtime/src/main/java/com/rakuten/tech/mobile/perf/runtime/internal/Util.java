package com.rakuten.tech.mobile.perf.runtime.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import javax.security.auth.x500.X500Principal;

/**
 * Shared functions for Relay SDKs.
 */
@RestrictTo(LIBRARY)
@VisibleForTesting(otherwise = PACKAGE_PRIVATE)
public final class Util {

  private static final String SUBSCRIPTION_META_KEY =
      "com.rakuten.tech.mobile.relay.SubscriptionKey";
  private static final String RELAY_APP_ID = "com.rakuten.tech.mobile.relay.AppId";
  private static final X500Principal DEBUG_DN = new X500Principal(
      "C=US, O=Android, CN=Android Debug");

  private Util() {
  }

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
  /* default */ static @Nullable String getSubscriptionKey(@NonNull final Context context) {
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
  /* default */ static @Nullable String getRelayAppId(@NonNull final Context context) {
    return getMeta(context, RELAY_APP_ID);
  }

  /* default */ static @Nullable String getMeta(@NonNull final Context context,
      @NonNull final String key) {
    try {
      Bundle metaData = context.getPackageManager().getApplicationInfo(context.getPackageName(),
          PackageManager.GET_META_DATA).metaData;
      return metaData != null ? metaData.getString(key) : null;
    } catch (PackageManager.NameNotFoundException e) {
      return null;
    }
  }

  /**
   * Check if the application is debuggable.
   *
   * <p>This method check if the application is signed by a debug key.
   * https://stackoverflow.com/questions/7085644/how-to-check-if-apk-is-signed-or-debug-build
   *
   * @param context application context
   * @return true if app is debuggable, false otherwise
   */
  @RestrictTo(LIBRARY)
  @VisibleForTesting(otherwise = PACKAGE_PRIVATE)
  public static boolean isAppDebuggable(@NonNull final Context context) {
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo = packageManager
          .getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
      Signature[] signatures = packageInfo.signatures;

      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      for (Signature signature : signatures) {
        ByteArrayInputStream stream = new ByteArrayInputStream(signature.toByteArray());
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(stream);
        boolean debuggable = DEBUG_DN.equals(cert.getSubjectX500Principal());
        if (debuggable) {
          return true;
        }
      }
    } catch (Exception e) {
      // Things went south, anyway the app is not debuggable.
    }
    return false;
  }
}
