package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.Bundle;
import com.rakuten.tech.mobile.perf.core.base.SupportV4FragmentBase;

// this is test data only for mixin, not rebasing, so parent class is SupportV4FragmentBase,
// not Fragment (from support v4)
public class ChildOfSupportV4Fragment extends SupportV4FragmentBase {

  public void onCreate(Bundle savedInstanceState) {

  }

}
