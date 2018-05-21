package com.rakuten.tech.mobile.perf.rewriter.mixins

import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.*
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class MixerSpec {
  ClassVisitor classVisitor
  Mixin mixin
  Mixer mixer
  ClassProvider classProvider
  final String volleyToolBoxPkg = "com.android.volley.toolbox"

  @Before
  void setup() {
    classProvider = new ClassProvider(resourceFile("performance-tracking-core.jar").absolutePath)
    classVisitor = new ClassWriter(classProvider, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = jar.getClassNode("${mixinPkg}.VolleyHurlStackMixin")
    MixinLoader mixinLoader = new MixinLoader(testLogger())
    mixin = mixinLoader.loadMixin(classNode)
    mixer = new Mixer(testLogger())
  }

  @Test
  void "should call rewriter method if class type match"() {
    Mixin mixinMock = spy(mixin)
    mixer.add(mixinMock)
    Class<?> clazz = classProvider.getClass("${volleyToolBoxPkg}.HurlStack");

    mixer.rewrite(clazz, classVisitor)

    verify(mixinMock).rewrite(any())
  }

  @Test
  void "should not call rewriter method if class type mismatch"() {
    Mixin mixinMock = spy(mixin)
    mixer.add(mixinMock)
    Class<?> clazz = classProvider.getClass("${mixinPkg}.ActivityMixin");

    mixer.rewrite(clazz, classVisitor)

    verify(mixinMock, never()).rewrite(any())
  }

  @Test
  void "should still return the same class visitor if class type mismatch"() {
    mixer.add(mixin)
    Class<?> clazz = classProvider.getClass("${mixinPkg}.ActivityMixin");

    ClassVisitor classVisitor = mixer.rewrite(clazz, this.classVisitor)

    assert classVisitor == this.classVisitor
  }

  @Test
  void "should return the a new class visitor if class type match"() {
    mixer.add(mixin)
    Class<?> clazz = classProvider.getClass("${volleyToolBoxPkg}.HurlStack");

    ClassVisitor classVisitor = mixer.rewrite(clazz, this.classVisitor)

    assert classVisitor != this.classVisitor
  }
}