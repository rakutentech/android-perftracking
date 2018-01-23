package com.rakuten.tech.mobile.perf.runtime.shadow;

import android.content.Context;
import com.rakuten.tech.mobile.perf.runtime.internal.Util;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Util.class)
public class UtilShadow {

  public static boolean mockDebugBuild = false;

  @Implementation
  public static boolean isAppDebuggable(Context context) {
    return mockDebugBuild;
  }

}
