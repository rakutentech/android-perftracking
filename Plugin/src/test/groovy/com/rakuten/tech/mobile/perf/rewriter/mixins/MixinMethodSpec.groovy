package com.rakuten.tech.mobile.perf.rewriter.mixins

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Type
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode

import static com.rakuten.tech.mobile.perf.TestUtil.*
import static org.mockito.ArgumentMatchers.*
import static org.mockito.Mockito.*

public class MixinMethodSpec extends UnitSpec {

  ClassJar jar
  ClassProvider provider
  MixinLoader mixinLoader
  ClassWriter writer

  @Before def void setup() {
    jar = new ClassJar(resourceFile("usertestui.jar"))
    provider = new ClassProvider(resourceFile("usertestui.jar").absolutePath)
    mixinLoader = new MixinLoader(testLogger())
    writer = new ClassWriter(provider, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
  }

  @Test def void "should invoke visit method on the provided class parameters"() {
    def mixinName = "${mixinPkg}.AdapterViewOnItemClickListenerMixin"
    Mixin mixin = mixinLoader.loadMixin(jar.getClassNode(mixinName))
    ClassVisitor visitorMock = spy(mixin.rewrite(provider.getClass(mixinName), writer))
    ClassReader reader = jar.getClassReader(mixinName)

    reader.accept(visitorMock, 0)

    verify(visitorMock).visit(anyInt(), anyInt(),
        eq("com/rakuten/tech/mobile/perf/core/mixins/AdapterViewOnItemClickListenerMixin"),
        any(), anyString(), any(String[]))
  }

  @Test
  def void "should invoke visit method on the provided class parameters, and visit instance fields if exists withing the method"() {
    def mixinName = "${mixinPkg}.ActivityMixin"
    Mixin mixin = mixinLoader.loadMixin(jar.getClassNode(mixinName))
    ClassVisitor visitorMock = spy(mixin.rewrite(provider.getClass(mixinName), writer))
    ClassReader reader = jar.getClassReader(mixinName)

    reader.accept(visitorMock, 0)

    verify(visitorMock).visit(anyInt(), anyInt(),
        eq("com/rakuten/tech/mobile/perf/core/mixins/ActivityMixin"),
        any(), anyString(), any(String[]))
  }

  @Test
  def void "Should call add method of MixinField class, If any Field Node exists in input classNode"() {
    ClassNode classNode = jar.getClassNode("${mixinPkg}.VolleyHurlStackMixin")
    classNode.fields = [
        createFieldNode("testMethodName", Type.OBJECT, [
            new AnnotationNode("Lcom/rakuten/tech/mobile/perf/core/annotations/AddField;")
        ])
    ]
    Mixin mixin = mixinLoader.loadMixin(classNode)
    MixinField mixinFieldMock = spy(mixin.fields.get(0))
    mixin.fields.add(0, mixinFieldMock)
    Class clazz = provider.getClass("${mixinPkg}.VolleyHurlStackMixin")
    ClassReader reader = jar.getClassReader("${mixinPkg}.VolleyHurlStackMixin")
    ClassVisitor visitor = mixin.rewrite(clazz, writer)

    reader.accept(visitor, 0)

    verify(mixinFieldMock).add(any(ClassVisitor))
  }

  private static FieldNode createFieldNode(def name, def desc, def visibleAnnotations) {
    FieldNode fieldNode = mock(FieldNode)
    fieldNode.name = name
    fieldNode.desc = desc
    fieldNode.visibleAnnotations = visibleAnnotations
    return fieldNode
  }
}