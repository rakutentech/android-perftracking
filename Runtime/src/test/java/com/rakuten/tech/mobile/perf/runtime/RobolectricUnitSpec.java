package com.rakuten.tech.mobile.perf.runtime;

import com.rakuten.tech.mobile.perf.BuildConfig;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@Ignore("This is base class for Robolectric tests")
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class RobolectricUnitSpec {

  @Before
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
  }
}
