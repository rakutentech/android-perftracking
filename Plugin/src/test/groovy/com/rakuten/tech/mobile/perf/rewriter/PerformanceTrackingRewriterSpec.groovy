package com.rakuten.tech.mobile.perf.rewriter

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile

public class PerformanceTrackingRewriterSpec extends UnitSpec {
  @Rule public final TemporaryFolder projectDir = new TemporaryFolder()
  PerformanceTrackingRewriter performanceTrackingRewriter

  @Before def void setup() {
    performanceTrackingRewriter = new PerformanceTrackingRewriter();
    performanceTrackingRewriter.compileSdkVersion = "android-23"
    performanceTrackingRewriter.classpath = resourceFile("android23.jar").absolutePath
    performanceTrackingRewriter.input = resourceFile("usertestui.jar").absolutePath
    performanceTrackingRewriter.outputJar = projectDir.newFile("output.jar")
    performanceTrackingRewriter.tempJar = projectDir.newFile("temp.jar")
  }

  @Test
  def void "should copy the content of input jar file to output jar file with transformation"() {
    performanceTrackingRewriter.rewrite()

    ClassJar temp = new ClassJar(new File(performanceTrackingRewriter.outputJar));
    ClassNode classNode = temp.getClassNode("jp.co.rakuten.sdtd.user.ui.BaseActivity")
    def instrumentedMethod = classNode.methods.find {
      it.name == "com_rakuten_tech_mobile_perf_onCreate"
    }
    assert instrumentedMethod
  }

  @Test
  def void "should rewrite AppPerformanceConfig class, set enable value to true and add to output JAR"() {
    performanceTrackingRewriter.input = resourceFile("TestAppPerformanceConfig.jar").absolutePath

    performanceTrackingRewriter.rewrite()

    ClassJar temp = new ClassJar(new File(performanceTrackingRewriter.outputJar));
    assert temp.hasClass("com.rakuten.tech.mobile.perf.runtime.internal.AppPerformanceConfig")
    ClassNode classNode = temp.getClassNode("com.rakuten.tech.mobile.perf.runtime.internal.AppPerformanceConfig")
    assert classNode.fields.size() > 0
    def enabled = classNode.fields.find { it.name == "enabled" }
    assert enabled
    assert enabled.value
  }
}