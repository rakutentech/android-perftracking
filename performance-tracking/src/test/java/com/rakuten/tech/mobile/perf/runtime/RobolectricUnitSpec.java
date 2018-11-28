package com.rakuten.tech.mobile.perf.runtime;

import com.rakuten.tech.mobile.perf.BuildConfig;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
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
    Logger logger = LogManager.getLogManager().getLogger("okhttp3.mockwebserver.MockWebServer");
    if (logger != null) {
      logger.setLevel(Level.OFF);
    }
  }
}
