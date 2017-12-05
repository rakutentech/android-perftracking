package com.rakuten.tech.mobile.perf.rewriter.classes

import com.rakuten.tech.mobile.perf.UnitSpec
import org.junit.Before
import org.junit.Test

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile;

class ClassProviderSpec extends UnitSpec {
  final String existingClass = "com.rakuten.tech.mobile.perf.core.Sender"
  ClassProvider provider

  @Before void setup() {
    provider = new ClassProvider(resourceFile("usertestui.jar").absolutePath)
  }

  @Test void "should provide existing class"() {
    assert provider.getClass(existingClass)
  }

  @Test(expected = RuntimeException.class)
  void "should fail to provide for non-existing class"() {
    provider.getClass("some.made.up.Clazz")
  }

}