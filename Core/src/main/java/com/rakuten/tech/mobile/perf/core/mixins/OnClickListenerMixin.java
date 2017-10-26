package com.rakuten.tech.mobile.perf.core.mixins;

import android.view.View;
import android.view.View.OnClickListener;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixImplementationOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;

@MixImplementationOf(OnClickListener.class)
public class OnClickListenerMixin {

  @ReplaceMethod
  public void onClick(View view) {
    Tracker.endMetric();

    int id = Tracker.startMethod(this, "onClick");
    try {
      onClick(view);
    } finally {
      Tracker.endMethod(id);
    }
  }
}
