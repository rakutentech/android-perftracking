package com.rakuten.tech.mobile.perf.rewriter.classes

import com.rakuten.tech.mobile.perf.UnitSpec
import org.junit.Before
import org.junit.Test

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile

class ClassJarSpec extends UnitSpec {
  ClassJar jar

  final String existingClass = "com.rakuten.tech.mobile.perf.core.Sender"

  @Before void setup() {
    jar = new ClassJar(resourceFile("usertestui.jar"))
  }

  @Test void "should read jar content"() {
    assert jar.getJarFile()
    assert jar.getClasses()
    assert jar.getClasses().size()
  }

  @Test void "should lookup class in jar"() {
    assert !jar.hasClass("some.made.up.Clazz")
    assert jar.hasClass(existingClass)
  }

  @Test void "should provide ClassReader"() {
    assert jar.getClassReader(existingClass)
  }

  @Test void "should provide ClassNode"() {
    assert jar.getClassNode(existingClass)
  }

  @Test(expected = RuntimeException)
  void "should fail to instantiate with invalid jar file"() {
    new ClassJar(null)
  }

  @Test(expected = RuntimeException)
  void "should fail to provide ClassReader with invalid jar file"() {
    jar.getClassReader(null)
  }
}