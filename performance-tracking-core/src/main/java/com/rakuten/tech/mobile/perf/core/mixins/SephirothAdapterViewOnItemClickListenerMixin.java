package com.rakuten.tech.mobile.perf.core.mixins;

import android.view.View;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.Exists;
import com.rakuten.tech.mobile.perf.core.annotations.MixImplementationOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.AdapterView.OnItemClickListener;

@Exists(OnItemClickListener.class)
@MixImplementationOf(OnItemClickListener.class)
public class SephirothAdapterViewOnItemClickListenerMixin {

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
