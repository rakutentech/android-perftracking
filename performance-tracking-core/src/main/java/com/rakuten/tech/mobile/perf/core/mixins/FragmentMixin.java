package com.rakuten.tech.mobile.perf.core.mixins;

import android.app.Fragment;
import android.os.Bundle;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MinCompileSdkVersion;
import com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;
import com.rakuten.tech.mobile.perf.core.base.FragmentBase;

@MinCompileSdkVersion(11)
@MixSubclassOf(Fragment.class)
public class FragmentMixin extends FragmentBase {

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

