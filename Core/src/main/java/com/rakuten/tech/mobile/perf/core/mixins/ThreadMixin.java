package com.rakuten.tech.mobile.perf.core.mixins;

import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;

@MixSubclassOf(Thread.class)
public class ThreadMixin {

  @ReplaceMethod
  public void run() {
    int id = Tracker.startMethod(this, "run");
    try {
      run();
    } finally {
      Tracker.endMethod(id);
    }
  }
}