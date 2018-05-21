package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.view.View;
import com.rakuten.tech.mobile.perf.core.Tracker;

public class ImplementationOfOnClickListenerPost implements View.OnClickListener {

  public void onClick(View view) {
    Tracker.endMetric();

    int id = Tracker.startMethod(this, "onClick");
    try {
      this.com_rakuten_tech_mobile_perf_onClick(view);
    } finally {
      Tracker.endMethod(id);
    }
  }

  private void com_rakuten_tech_mobile_perf_onClick(View view) {

  }
}
