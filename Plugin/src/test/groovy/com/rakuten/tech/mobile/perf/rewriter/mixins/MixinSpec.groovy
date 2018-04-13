package com.rakuten.tech.mobile.perf.rewriter.mixins

import android.app.Activity
import android.webkit.WebChromeClient
import com.android.volley.toolbox.HurlStack
import com.rakuten.tech.mobile.perf.rewriter.bytecode.ByteCodeTestData
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.testdata.mixins.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.junit.runners.Parameterized.Parameters
import org.objectweb.asm.ClassReader
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile
import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static com.rakuten.tech.mobile.perf.rewriter.bytecode.ByteCodeUtils.*

class MixinSpec {

  Mixin mixin

  @Before void setup() {
    mixin = new Mixin(testLogger())
  }

  // ---- mixin matching rules by example ----

  @Test
  void "can target class explicitly"() {
    mixin.targetClass = 'java.lang.Object'

    assert  mixin.match(Object.class)
    assert !mixin.match(String.class) // no subclasses
  }

  @Test
  void "can target subclasses of a class"() {
    mixin.targetSubclassOf = 'java.util.AbstractList'

    // excluding parent classes
    assert !mixin.match(Object.class)
    assert !mixin.match(AbstractCollection.class)
    // excluding the class itself
    assert !mixin.match(AbstractList.class)
    // including direct subclasses
    assert  mixin.match(AbstractSequentialList.class)
    assert  mixin.match(ArrayList.class)
    // including indirect subclasses
    assert  mixin.match(LinkedList.class)
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

  // ---- expected byte code weaving ----

  @RunWith(Parameterized)
  static class ShouldMixin {
    private Mixin mixin
    private ByteCodeTestData testData

    @Parameters(name = '{0} should mixin {1}')
    static Collection<Object[]> data() { HurlStack
      return [
          ['ActivityMixin', 'mixins/ChildOfActivity'],
          ['AdapterViewOnItemClickListenerMixin', 'mixins/ImplementationOfOnItemClickListener'],
          ['AsyncTaskMixin', 'mixins/ChildOfAsyncTask'],
          ['FragmentMixin', 'mixins/ChildOfFragment'],
          ['OnClickListenerMixin', 'mixins/ImplementationOfOnClickListener'],
          ['RunnableMixin', 'mixins/ImplementationOfRunnable'],
          ['SephirothAdapterViewOnItemClickListenerMixin', 'mixins/ImplementationOfSephirothOnItemClickListener'],
          ['SupportV4FragmentMixin', 'mixins/ChildOfSupportV4Fragment'],
          ['ThreadMixin', 'mixins/ChildOfThread'],
          // Volley Hurl Stack mixin is a bit hard to test because we need the actual class
          // putting volley as test dependency won't work because the class will collide
          // with the HurlStack from stubs 😢.
          ['WebViewClientMixin', 'mixins/ChildOfWebViewClient'],
          ['WebChromeClientMixin', 'mixins/ChildOfWebChromeClient']
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

  // ---- expect no byte code weaving ----

  @RunWith(Parameterized)
  static class ShouldNotMixin {
    private Mixin mixin
    private List<ClassReader> testData

    @Parameters(name = '{0} should NOT mixin {1}')
    static Collection<Object[]> data() {
      return [
          [
              'RunnableMixin',
              [
                  'mixins/AbstractImplementationOfRunnable',
                  'mixins/ChildOfArrayList',
                  'mixins/ChildOfActivity',
                  'mixins/ImplementationOfOnItemClickListener',
                  'mixins/ChildOfAsyncTask'
              ]
          ],
          [
              'AsyncTaskMixin',
              [
                  'mixins/AbstractImplementationOfRunnable',
                  'mixins/ChildOfArrayList',
                  'mixins/ChildOfActivity',
                  'mixins/ImplementationOfOnItemClickListener',
                  'mixins/ImplementationOfRunnable'
              ]
          ]

      ]*.toArray()
    }

    ShouldNotMixin(String mixinClassName, List<String> testClasesNames) {
      this.mixin = loadMixinFromCore(mixinClassName)
      this.testData = testClasesNames.collect { readClass(it) }
    }

    @Test
    void "mixin should NOT instrument bytecode"() {
      testData.forEach {
        def originalClassBytes = readClassByteCode(it)
        def mixedClassBytes = readClassByteCode(it, mixin.&rewrite)

        // write bytecode for manual inspection
        def testFile = writeClassToFile("${it}Actual", mixedClassBytes)

        assert originalClassBytes == mixedClassBytes
        assertThatBytecode(originalClassBytes).isEqualTo(mixedClassBytes)

        testFile.delete() // assertions pass, clean up bytecode
      }
    }
  }

  // ---- mixin matching with some real classes ----

  @RunWith(Parameterized)
  static class MixinShouldMatch {
    private Mixin mixin
    private List<Class> classesThatShouldMatch
    private List<Class> classesThatShouldNotMatch


    @Parameters(name = 'mixin {0} x should match {1}, should not match {2}')
    static Collection<Object[]> data() {
      return [
          [
              'VolleyHurlStackMixin',
              [HurlStack.class],
              [Thread.class, WebChromeClient.class, Activity.class]
          ],
          [
              'AdapterViewOnItemClickListenerMixin',
              [ImplementationOfOnItemClickListener.class],
              [Thread.class, HurlStack.class, WebChromeClient.class, Activity.class]
          ],
          [
              'AsyncTaskMixin',
              [ChildOfAsyncTask.class],
              [Thread.class, HurlStack.class, WebChromeClient.class, Activity.class]
          ],
          [
              'RunnableMixin',
              [Thread.class, ImplementationOfRunnable.class, AbstractImplementationOfRunnable],
              [HurlStack.class, WebChromeClient.class, Activity.class]
          ],
          [
              'ActivityMixin',
              [ChildOfActivity.class],
              [WebChromeClient.class, HurlStack.class, Thread.class, Activity.class]
          ]
      ]*.toArray()
    }

    MixinShouldMatch(String mixinClass, List<Class> classesThatShouldMatch,
                     List<Class> classesThatShouldNotMatch) {
      this.mixin = loadMixinFromCore(mixinClass)
      this.classesThatShouldMatch = classesThatShouldMatch
      this.classesThatShouldNotMatch = classesThatShouldNotMatch
    }

    @Test
    void "mixin should match target class"() {
      classesThatShouldMatch.forEach {
        assert mixin.match(it)
      }

      classesThatShouldNotMatch.forEach {
        assert !mixin.match(it)
      }
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
}
