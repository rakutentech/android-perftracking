package com.rakuten.tech.mobile.perf.rewriter.classes

import com.rakuten.tech.mobile.perf.UnitSpec
import org.gradle.api.logging.Logging
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile


class ClassTrimmerSpec extends UnitSpec {
  ClassTrimmer classTrimmer

  @RunWith(Parameterized)
  static class ClassTrimmerSpecParameterized extends UnitSpec {
    private String input

    @Parameters( name = "input = {0}") static Collection<Object[]> data() {
      return [[""], ["test"], [null]]*.toArray()
    }

    ClassTrimmerSpecParameterized(final String input) {
      this.input = input
    }

    @Test(expected = RuntimeException)
    void "should fail to instantiate with invalid compileSdkVersion"() {
      ClassProvider provider = new ClassProvider(resourceFile("usertestui.jar").absolutePath)
      classTrimmer = new ClassTrimmer(input, provider, Logging.getLogger(ClassTrimmerSpec.class.getName()))
    }
  }


  @Before void setup() {
    ClassProvider provider = new ClassProvider(resourceFile("usertestui.jar").absolutePath)
    classTrimmer = new ClassTrimmer("android-23", provider, Logging.getLogger(ClassTrimmerSpec.class.getName()))
  }

  @Test void "should trim methods which has annotation"() {
    ClassJar classJar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = classJar.getClassNode("com.rakuten.tech.mobile.perf.core.base.FragmentBase")
    int originalMethodSize = classNode.methods.size()
    classTrimmer.trim(classNode)
    assert classNode.methods.size() != originalMethodSize
  }

  @Test void "should not trim methods which does not have annotation"() {
    ClassJar classJar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = classJar.getClassNode("com.rakuten.tech.mobile.perf.core.base.WebChromeClientBase")
    int originalMethodSize = classNode.methods.size()
    classTrimmer.trim(classNode)
    assert classNode.methods.size() == originalMethodSize
  }

  @Test void "should trim methods which has annotation MaxCompileSdkVersion"() {
    ClassJar classJar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = classJar.getClassNode("com.rakuten.tech.mobile.perf.core.base.SupportV4FragmentBase")
    int originalMethodSize = classNode.methods.size()
    classTrimmer.trim(classNode)
    assert classNode.methods.size() != originalMethodSize
  }
}