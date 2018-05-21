package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.Bundle;
import android.os.PersistableBundle;
import com.rakuten.tech.mobile.perf.core.base.ActivityBase;

// this is test data only for mixin, not rebasing, so parent class is ActivityBase, not Activity
public class ChildOfActivity extends ActivityBase {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Override public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
    super.onCreate(savedInstanceState, persistentState);
  }

  @Override public void onBackPressed() {
    super.onBackPressed();
  }
}
