package com.rakuten.tech.mobile.perf.rewriter.mixins

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile
import static com.rakuten.tech.mobile.perf.TestUtil.testLogger

class MixinSpec extends UnitSpec {

  Mixin mixin

  @Before def void setup() {
    mixin = new Mixin(testLogger())
  }

  @Test
  def void "should return false if the input class does not belong to com.rakuten.tech.mobile.perf.core.mixins "() {
    def match = mixin.match(Object.class)

    assert !match
  }

  @RunWith(Parameterized)
  static class MixinMatcherPositiveSpec extends UnitSpec {
    private String mixinInput
    private String classInput

    @Parameters static Collection<Object[]> data() {
      return [
          ["com.rakuten.tech.mobile.perf.core.mixins.AdapterViewOnItemClickListenerMixin", "com.rakuten.tech.mobile.perf.core.mixins.TestTargetImplementationOf"],
          ["com.rakuten.tech.mobile.perf.core.mixins.ActivityMixin", "com.rakuten.tech.mobile.perf.core.mixins.TestTargetSubClassOf"],
          ["com.rakuten.tech.mobile.perf.core.mixins.VolleyHurlStackMixin", "com.android.volley.toolbox.HurlStack"]
      ]*.toArray()
    }

    public MixinMatcherPositiveSpec(final String mixinInput, final String classInput) {
      this.mixinInput = mixinInput
      this.classInput = classInput
    }

    @Test
    void "should match the input mixin object with the class input as mixin objects conditions are satisfied"() {
      ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
      ClassProvider provider = new ClassProvider(resourceFile("Core.jar").absolutePath)
      ClassNode classNode = jar.getClassNode(mixinInput)
      MixinLoader mixinLoader = new MixinLoader(testLogger())
      Mixin mixin = mixinLoader.loadMixin(classNode)

      def match = mixin.match(provider.getClass(classInput))

      assert match
    }
  }

  @RunWith(Parameterized)
  static class MixinMatcherNegativeSpec extends UnitSpec {
    private String mixinInput
    private String classInput

    @Parameters static Collection<Object[]> data() {
      return [
          ["com.rakuten.tech.mobile.perf.core.mixins.AdapterViewOnItemClickListenerMixin", "java.lang.Thread"],
          ["com.rakuten.tech.mobile.perf.core.mixins.ActivityMixin", "android.webkit.WebChromeClient"],
          ["com.rakuten.tech.mobile.perf.core.mixins.VolleyHurlStackMixin", "android.app.Activity"]
      ]*.toArray()
    }

    public MixinMatcherNegativeSpec(final String mixinInput, final String classInput) {
      this.mixinInput = mixinInput
      this.classInput = classInput
    }

    @Test
    void "should not match the input mixin object with the class input as mixin objects conditions are not satisfied"() {
      ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
      ClassProvider provider = new ClassProvider(resourceFile("Core.jar").absolutePath)
      ClassNode classNode = jar.getClassNode(mixinInput)
      MixinLoader mixinLoader = new MixinLoader(testLogger())
      Mixin mixin = mixinLoader.loadMixin(classNode)

      def match = mixin.match(provider.getClass(classInput))

      assert !match
    }
  }
}