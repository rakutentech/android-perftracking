package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.view.View;
import com.rakuten.tech.mobile.perf.core.Tracker;
import it.sephiroth.android.library.widget.AdapterView;


public class ImplementationOfSephirothOnItemClickListenerPost implements
    AdapterView.OnItemClickListener {

  public void onItemClick(AdapterView<?> parent, View view, int position, long itemId) {
    Tracker.endMetric();

    int id = Tracker.startMethod(this, "onItemClick");
    try {
      this.com_rakuten_tech_mobile_perf_onItemClick(parent, view, position, itemId);
    } finally {
      Tracker.endMethod(id);
    }

  }

  private void com_rakuten_tech_mobile_perf_onItemClick(AdapterView<?> parent, View view, int
      position, long itemId) {

  }

}
