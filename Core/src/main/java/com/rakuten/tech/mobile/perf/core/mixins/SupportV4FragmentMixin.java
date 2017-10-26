package com.rakuten.tech.mobile.perf.core.mixins;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.Exists;
import com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import com.rakuten.tech.mobile.perf.core.base.SupportV4FragmentBase;

@Exists(Fragment.class)
@MixSubclassOf(Fragment.class)
public class SupportV4FragmentMixin extends SupportV4FragmentBase {

  @ReplaceMethod
  public void onCreate(Bundle savedInstanceState) {
    if (!com_rakuten_tech_mobile_perf_onCreate_tracking) {
      com_rakuten_tech_mobile_perf_onCreate_tracking = true;

      int id = Tracker.startMethod(this, "onCreate");

      try {
        onCreate(savedInstanceState);
      } finally {
        Tracker.endMethod(id);
        com_rakuten_tech_mobile_perf_onCreate_tracking = false;
      }
    } else {
      onCreate(savedInstanceState);
    }
  }
}
