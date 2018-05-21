package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.Bundle;
import android.os.PersistableBundle;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.base.ActivityBase;

public class ChildOfActivityPost extends ActivityBase {

  protected void onCreate(Bundle savedInstanceState) {
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


  public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
    if (!this.com_rakuten_tech_mobile_perf_onCreate_tracking) {
      this.com_rakuten_tech_mobile_perf_onCreate_tracking = true;

      int id = Tracker.startMethod(this, "onCreate");

      try {
        this.com_rakuten_tech_mobile_perf_onCreate(savedInstanceState, persistentState);
      } finally {
        Tracker.endMethod(id);
        this.com_rakuten_tech_mobile_perf_onCreate_tracking = false;
      }
    } else {
      this.com_rakuten_tech_mobile_perf_onCreate(savedInstanceState, persistentState);
    }
  }

  private void com_rakuten_tech_mobile_perf_onCreate(Bundle savedInstanceState,
      PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);
  }


  public void onBackPressed() {
    Tracker.endMetric();
    this.com_rakuten_tech_mobile_perf_onBackPressed();
  }

  private void com_rakuten_tech_mobile_perf_onBackPressed() {
    super.onBackPressed();
  }
}
