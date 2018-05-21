package com.rakuten.tech.mobile.perf.core.mixins;

import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixImplementationOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;

@MixImplementationOf(OnItemClickListener.class)
public class AdapterViewOnItemClickListenerMixin {

  @ReplaceMethod
  public void onItemClick(AdapterView<?> parent, View view, int position, long itemId) {
    Tracker.endMetric();

    int id = Tracker.startMethod(this, "onItemClick");
    try {
      onItemClick(parent, view, position, itemId);
    } finally {
      Tracker.endMethod(id);
    }
  }
}
