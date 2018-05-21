package com.rakuten.tech.mobile.perf.core.mixins;

import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixImplementationOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;

@MixImplementationOf(Runnable.class)
public class RunnableMixin {

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