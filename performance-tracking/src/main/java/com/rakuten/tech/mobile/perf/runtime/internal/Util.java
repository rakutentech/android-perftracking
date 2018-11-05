package com.rakuten.tech.mobile.perf.runtime.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY;
import static android.support.annotation.VisibleForTesting.PACKAGE_PRIVATE;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

/** Shared functions for Relay SDKs. */
@RestrictTo(LIBRARY)
@VisibleForTesting(otherwise = PACKAGE_PRIVATE)
public final class Util {

  private Util() {}

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
  static boolean isAppDebuggable(@NonNull final Context context) {
    try {
      PackageManager packageManager = context.getPackageManager();
      PackageInfo packageInfo =
          packageManager.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
      Signature[] signatures = packageInfo.signatures;

      CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");

      for (Signature signature : signatures) {
        ByteArrayInputStream stream = new ByteArrayInputStream(signature.toByteArray());
        X509Certificate cert = (X509Certificate) certificateFactory.generateCertificate(stream);
        String principal = cert.getSubjectX500Principal().toString().toUpperCase();
        return principal.contains("C=US")
            && principal.contains("O=ANDROID")
            && principal.contains("CN=ANDROID DEBUG");
      }
    } catch (Exception e) {
      // Things went south, anyway the app is not debuggable.
    }
    return false;
  }
}
