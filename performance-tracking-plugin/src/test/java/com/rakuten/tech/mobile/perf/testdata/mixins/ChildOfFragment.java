package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.Bundle;
import com.rakuten.tech.mobile.perf.core.base.FragmentBase;

// this is test data only for mixin, not rebasing, so parent class is FragmentBase, not Fragment
public class ChildOfFragment extends FragmentBase {

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

}
