package com.rakuten.tech.mobile.perf.rewriter

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile

public class DummyRewriterSpec extends UnitSpec {
  @Rule public final TemporaryFolder projectDir = new TemporaryFolder()
  DummyRewriter dummyRewriter

  @Before def void setup() {
    dummyRewriter = new DummyRewriter();
    dummyRewriter.compileSdkVersion = "android-23"
    dummyRewriter.classpath = resourceFile("android23.jar").absolutePath
    dummyRewriter.input = resourceFile("usertestui.jar").absolutePath
    dummyRewriter.outputJar = projectDir.newFile("output.jar")
    dummyRewriter.tempJar = projectDir.newFile("temp.jar")
  }

  @Test
  def void "should copy the content of input jar file to output jar file with out transformation"() {
    dummyRewriter.rewrite()

    ClassJar temp = new ClassJar(new File(dummyRewriter.outputJar));
    ClassNode classNode = temp.getClassNode("jp.co.rakuten.sdtd.user.ui.BaseActivity")
    def instrumentedMethod = classNode.methods.find {
      it.name == "com_rakuten_tech_mobile_perf_onCreate"
    }
    assert !instrumentedMethod
  }

  @Test
  def void "should rewrite AppPerformanceConfig class, set enable value to false and add to output JAR"() {
    dummyRewriter.input = resourceFile("TestAppPerformanceConfig.jar").absolutePath

    dummyRewriter.rewrite()

    ClassJar temp = new ClassJar(new File(dummyRewriter.outputJar));
    assert temp.hasClass("com.rakuten.tech.mobile.perf.runtime.internal.AppPerformanceConfig")
    ClassNode classNode = temp.getClassNode("com.rakuten.tech.mobile.perf.runtime.internal.AppPerformanceConfig")
    assert classNode.fields.size() > 0
    def enabled = classNode.fields.find { it.name == "enabled" }
    assert enabled
    assert !enabled.value
  }
}