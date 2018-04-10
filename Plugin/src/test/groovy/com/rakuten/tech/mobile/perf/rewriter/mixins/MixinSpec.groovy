package com.rakuten.tech.mobile.perf.rewriter.mixins

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.bytecode.ByteCodeTestData
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import com.rakuten.tech.mobile.perf.testdata.mixins.ChildOfArrayList
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile
import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static com.rakuten.tech.mobile.perf.rewriter.bytecode.ByteCodeUtils.*

class MixinSpec extends UnitSpec {

  Mixin mixin

  @Before void setup() {
    mixin = new Mixin(testLogger())
  }

  @RunWith(Parameterized)
  static class MixinMatchingIntegrationSpec extends UnitSpec {
    private String mixinClassName
    private String targetClassName
    private boolean shouldMatch


    @Parameters(name = 'mixin {0} x target class {1} => should match {2}')
    static Collection<Object[]> data() {
      return [
          ['VolleyHurlStackMixin', 'com.android.volley.toolbox.HurlStack', true],
          ['AdapterViewOnItemClickListenerMixin', 'java.lang.Thread', false],
          ['ActivityMixin', 'android.webkit.WebChromeClient', false],
          ['VolleyHurlStackMixin', 'android.app.Activity', false]
      ]*.toArray()
    }

    MixinMatchingIntegrationSpec(String mixinClass, String targetClass, boolean shouldMatch) {
      this.mixinClassName = mixinClass
      this.targetClassName = targetClass
      this.shouldMatch = shouldMatch
    }

    @Test
    void "should match the input mixin object with the class input as mixin objects conditions are satisfied"() {
      Class targetClass = loadClassFromJar(targetClassName, "usertestui.jar")
      Mixin mixin = loadMixinFromCore(mixinClassName)

      def match = mixin.match(targetClass)

      assert match == shouldMatch
    }
  }

  @Test
  void "can target class explicitly"() {
    mixin.targetClass = 'java.lang.Object'

    assert  mixin.match(Object.class)
    assert !mixin.match(String.class) // no subclasses
  }

  @Test
  void "can target subclasses of a class"() {
    mixin.targetSubclassOf = 'java.lang.Number'

    assert !mixin.match(Object.class) // excluding parent class
    assert !mixin.match(Number.class)  // excluding the class itself
    assert  mixin.match(Integer.class) // including subclasses
  }


  @Test
  void "can target implementations of an interface"() {
    mixin.targetImplementationOf = 'java.util.List'

    assert !mixin.match(List.class)     // excluding the interface itself
    assert  mixin.match(ArrayList.class) // including implementing classes
    assert !mixin.match(ChildOfArrayList.class) // excluding subclasses of implementing classes
    // TODO: consider if mixins shouldn't match subclasses of implementing classes as well
    // e.g. interface A <- implements - abstract class B <- subclass - class C
    // in this case the method implementation might be in class C but the mixin doesn't "match"
  }

  @Test
  void "Runnable mixin should not rewrite descendants of array list classes"() {
    def runnableReader = readClass('mixins/ChildOfArrayList')
    mixin = loadMixinFromCore('RunnableMixin')

    def originalClassBytes = readClassByteCode(runnableReader)
    def mixedClassBytes = readClassByteCode(runnableReader, mixin.&rewrite)

    assert originalClassBytes == mixedClassBytes
    assertThatBytecode(originalClassBytes).isEqualTo(mixedClassBytes)
  }

  @Test
  void "should not mixin to abstract methods"() {
    def runnableReader = readClass('mixins/AbstractImplementationOfRunnable')
    mixin = loadMixinFromCore('RunnableMixin')

    def originalClassBytes = readClassByteCode(runnableReader)
    def mixedClassBytes = readClassByteCode(runnableReader, mixin.&rewrite)

    def testFile = writeClassToFile("actual", mixedClassBytes)

    assert originalClassBytes == mixedClassBytes
    assertThatBytecode(originalClassBytes).isEqualTo(mixedClassBytes)

    testFile.delete()
  }

  @RunWith(Parameterized)
  static class ShouldMixin extends UnitSpec {
    private Mixin mixin
    private ByteCodeTestData testData

    @Parameters(name = '{0} x {1}')
    static Collection<Object[]> data() {
      return [
          ['AsyncTaskMixin', 'mixins/ChildOfAsyncTask'],
          ['AdapterViewOnItemClickListenerMixin', 'mixins/ImplementationOfOnItemClickListener'],
          ['RunnableMixin', 'mixins/ImplementationOfRunnable'],
          ['ActivityMixin', 'mixins/ChildOfActivity']
      ]*.toArray()
    }

    ShouldMixin(String mixinClassName, String testDataSetName) {
      this.mixin = loadMixinFromCore(mixinClassName)
      this.testData = testDataBytecode(testDataSetName)
    }

    @Test
    void "mixin should instrument bytecode"() {
      def mixedByteCode = readClassByteCode(testData.originalClass, mixin.&rewrite)

      testData.record(mixedByteCode) // write bytecode for manual inspection

      assert testData.originalByteCode != mixedByteCode
      assertThatBytecode(testData.expectedByteCode).isEqualTo(mixedByteCode)

      testData.clear() // assertions pass, clean up bytecode
    }
  }

  // utils

  /**
   * Loads a mixin instance as defined in Core (from freshly copied Core.jar) from package
   * {@link com.rakuten.tech.mobile.perf.core.mixins}
   *
   * @param mixinClassName name of the mixin (relative to package)
   * @return loaded mixin
   */
  static Mixin loadMixinFromCore(String mixinClassName) {
    ClassNode mixinClassNode = new ClassJar(resourceFile("Core.jar")).getClassNode(
        "com.rakuten.tech.mobile.perf.core.mixins.$mixinClassName")
    new MixinLoader(testLogger()).loadMixin(mixinClassNode)
  }

  /**
   * Load a class object from a jar that's in test resources
   * @param className fully qualified class name
   * @param jarName jar name
   * @return class object
   */
  static Class loadClassFromJar(String className, String jarName) {
    ClassProvider provider = new ClassProvider(resourceFile(jarName).absolutePath)
    provider.getClass(className)
  }
}
