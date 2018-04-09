package com.rakuten.tech.mobile.perf.testdata.mixins;

import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;

public class ImplementationOfRunnablePost implements Runnable {
  @ReplaceMethod
  public void run() {
    int id = Tracker.startMethod(this, "run");

    try {
      this.com_rakuten_tech_mobile_perf_run();
    } finally {
      Tracker.endMethod(id);
    }

  }

  private void com_rakuten_tech_mobile_perf_run() {
  }
}
