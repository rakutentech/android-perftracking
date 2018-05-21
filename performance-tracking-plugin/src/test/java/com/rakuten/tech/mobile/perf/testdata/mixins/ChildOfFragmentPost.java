package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.Bundle;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.base.FragmentBase;


public class ChildOfFragmentPost extends FragmentBase {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    if (!this.com_rakuten_tech_mobile_perf_onCreate_tracking) {
      this.com_rakuten_tech_mobile_perf_onCreate_tracking = true;

      int id = Tracker.startMethod(this, "onCreate");

      try {
        this.com_rakuten_tech_mobile_perf_onCreate(savedInstanceState);
      } finally {
        Tracker.endMethod(id);
        this.com_rakuten_tech_mobile_perf_onCreate_tracking = false;
      }
    } else {
      this.com_rakuten_tech_mobile_perf_onCreate(savedInstanceState);
    }
  }

  private void com_rakuten_tech_mobile_perf_onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }
}
