package com.rakuten.tech.mobile.perf.runtime.internal;

/**
 * Configuration to control the tracker initialization. Configuration gets replaced during
 * transformation based on build time switch.
 */
final class AppPerformanceConfig {

  // enabled variable cannot be declared as final static.
  // Because the compiler inlines final static variables, i.e. it replaces references with values
  // during compilation.
  public static boolean enabled = true;
}
