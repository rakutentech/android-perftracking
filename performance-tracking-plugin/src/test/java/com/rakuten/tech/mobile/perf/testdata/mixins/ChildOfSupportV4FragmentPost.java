package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.Bundle;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.base.SupportV4FragmentBase;

// this is test data only for mixin, not rebasing, so parent class is SupportV4FragmentBase,
// not Fragment (from support v4)
public class ChildOfSupportV4FragmentPost extends SupportV4FragmentBase {

  public void onCreate(Bundle savedInstanceState) {
    if (!com_rakuten_tech_mobile_perf_onCreate_tracking) {
      com_rakuten_tech_mobile_perf_onCreate_tracking = true;

      int id = Tracker.startMethod(this, "onCreate");

      try {
        this.com_rakuten_tech_mobile_perf_onCreate(savedInstanceState);
      } finally {
        Tracker.endMethod(id);
        com_rakuten_tech_mobile_perf_onCreate_tracking = false;
      }
    } else {
      this.com_rakuten_tech_mobile_perf_onCreate(savedInstanceState);
    }
  }

  private void com_rakuten_tech_mobile_perf_onCreate(Bundle savedInstanceState) {

  }

}
