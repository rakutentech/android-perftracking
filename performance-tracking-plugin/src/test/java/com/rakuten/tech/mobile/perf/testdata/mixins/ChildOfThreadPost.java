package com.rakuten.tech.mobile.perf.testdata.mixins;

import com.rakuten.tech.mobile.perf.core.Tracker;

public class ChildOfThreadPost extends Thread {

  public void run() {
    int id = Tracker.startMethod(this, "run");
    try {
      this.com_rakuten_tech_mobile_perf_run();
    } finally {
      Tracker.endMethod(id);
    }
  }

  private void com_rakuten_tech_mobile_perf_run() {
    super.run();
  }
}
