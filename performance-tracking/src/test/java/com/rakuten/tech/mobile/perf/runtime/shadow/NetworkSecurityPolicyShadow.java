package com.rakuten.tech.mobile.perf.runtime.shadow;

import android.security.NetworkSecurityPolicy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/** Workaround for robolectric + OkHttp, see https://github.com/square/okhttp/issues/2533. */
@SuppressWarnings("checkstyle:JavadocMethod")
@Implements(NetworkSecurityPolicy.class)
public class NetworkSecurityPolicyShadow {

  @SuppressWarnings("ClassNewInstance")
  @Implementation
  public static NetworkSecurityPolicy getInstance() {
    //noinspection OverlyBroadCatchBlock
    try {
      Class<?> shadow = Class.forName("android.security.NetworkSecurityPolicy");
      return (NetworkSecurityPolicy) shadow.newInstance();
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  @Implementation
  public boolean isCleartextTrafficPermitted(String hostname) {
    return true;
  }
}
