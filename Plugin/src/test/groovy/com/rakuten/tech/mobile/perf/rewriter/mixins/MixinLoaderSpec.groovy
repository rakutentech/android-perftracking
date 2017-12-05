package com.rakuten.tech.mobile.perf.rewriter.mixins

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.tree.AnnotationNode
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.FieldNode
import org.objectweb.asm.tree.MethodNode

import static com.rakuten.tech.mobile.perf.TestUtil.*

public class MixinLoaderSpec extends UnitSpec {
  MixinLoader mixinLoader
  ClassJar jar
  ClassNode classNode
  final String addFieldAnnotationDec = "Lcom/rakuten/tech/mobile/perf/core/annotations/AddField;"
  final String replaceMethodAnnotationDec = "Lcom/rakuten/tech/mobile/perf/core/annotations/ReplaceMethod;"
  final String testFieldName = "testFieldName"
  final String testMethodName = "testMethodName"

  @Before def void setup() {
    mixinLoader = new MixinLoader(testLogger())
    jar = new ClassJar(resourceFile("usertestui.jar"))
    classNode = jar.getClassNode("${mixinPkg}.VolleyHurlStackMixin")
  }

  @Test def void "should create Mixin Object for classNode with MixSubclassOf annotation"() {
    classNode = jar.getClassNode("${mixinPkg}.ActivityMixin")

    String subClassName = mixinLoader.loadMixin(classNode).targetSubclassOf

    assert subClassName != null
  }

  @Test def void "should create Mixin Object for classNode with MixImplementationOf annotation"() {
    classNode = jar.getClassNode("${mixinPkg}.AdapterViewOnItemClickListenerMixin")

    String targetImplementationName = mixinLoader.loadMixin(classNode).targetImplementationOf

    assert targetImplementationName != null
  }

  @Test def void "should create Mixin Object for classNode with MixClass annotation"() {
    String className = mixinLoader.loadMixin(classNode).mixinClass

    assert className != null
  }

  @Test
  def void "should create a mixin object with fields if the ClassNode contains visible annotations, but exclude null AnnotationNode"() {
    classNode.fields = [createFieldNode(testFieldName, [new AnnotationNode(addFieldAnnotationDec)], null),
                        createFieldNode(null, null, null)]

    Mixin mixin = mixinLoader.loadMixin(classNode)

    assert mixin.fields.get(0).name == testFieldName
    assert mixin.fields.size() == 1
  }

  @Test
  def void "should create a mixin object with fields if the ClassNode contains invisible annotation, but exclude null AnnotationNode"() {
    classNode.fields = [createFieldNode(testFieldName, null, [new AnnotationNode(addFieldAnnotationDec)]),
                        createFieldNode(null, null, null)]

    Mixin mixin = mixinLoader.loadMixin(classNode)

    assert mixin.fields.get(0).name == testFieldName
    assert mixin.fields.size() == 1
  }

  @Test
  def void "should create a mixin object with fields if the ClassNode contains visible annotations, but exclude empty AnnotationNodeList"() {
    classNode.fields = [createFieldNode(testFieldName, [new AnnotationNode(addFieldAnnotationDec)], null),
                        createFieldNode(null, [], null)]

    Mixin mixin = mixinLoader.loadMixin(classNode)

    assert mixin.fields.get(0).name == testFieldName
    assert mixin.fields.size() == 1
  }

  @Test
  def void "should create a mixin object with fields if the ClassNode contains invisible annotation, but exclude empty AnnotationNodeList"() {
    classNode.fields = [
        createFieldNode(testFieldName, null, [new AnnotationNode(addFieldAnnotationDec)]),
        createFieldNode(null, null, [])
    ]

    Mixin mixin = mixinLoader.loadMixin(classNode)

    assert mixin.fields.get(0).name == testFieldName
    assert mixin.fields.size() == 1
  }

  @Test
  def void "should create a mixin object with methods if the ClassNode contains visible annotations, but exclude null AnnotationNode"() {
    classNode.methods = [
        createMethodNode(testMethodName, [new AnnotationNode(replaceMethodAnnotationDec)], null),
        createMethodNode(null, null, null)
    ]

    int size = mixinLoader.loadMixin(classNode).methods.size()

    assert size == 1
  }

  @Test
  def void "should create a mixin object with methods if the ClassNode contains invisible annotation, but exclude null AnnotationNode"() {
    classNode.methods = [
        createMethodNode(testMethodName, null, [new AnnotationNode(replaceMethodAnnotationDec)]),
        createMethodNode(null, null, null)
    ]

    int size = mixinLoader.loadMixin(classNode).methods.size()

    assert size == 1
  }

  @Test
  def void "should create a mixin object with methods if the ClassNode contains visible annotations, but exclude empty AnnotationNodeList"() {
    classNode.methods = [
        createMethodNode(testMethodName, [new AnnotationNode(replaceMethodAnnotationDec)], null),
        createMethodNode(null, [], null)
    ]

    int size = mixinLoader.loadMixin(classNode).methods.size()

    assert size == 1
  }

  @Test
  def void "should create a mixin object with methods if the ClassNode contains invisible annotation, but exclude empty AnnotationNodeList"() {
    classNode.methods = [
        createMethodNode(testMethodName, null, [new AnnotationNode(replaceMethodAnnotationDec)]),
        createMethodNode(null, null, [])
    ]

    int size = mixinLoader.loadMixin(classNode).methods.size()

    assert size == 1
  }

  private static MethodNode createMethodNode(
      def name, def visibleAnnotationNodes, def invisibleAnnotationNodes) {
    MethodNode methodNode = new MethodNode(0, name, null, null, new String[0])
    methodNode.visibleAnnotations = visibleAnnotationNodes
    methodNode.invisibleAnnotations = invisibleAnnotationNodes
    return methodNode
  }

  private static FieldNode createFieldNode(
      def name, def visibleAnnotationNodes, def invisibleAnnotationNodes) {
    FieldNode fieldNode = new FieldNode(0, name, null, null, new Integer(1))
    fieldNode.visibleAnnotations = visibleAnnotationNodes
    fieldNode.invisibleAnnotations = invisibleAnnotationNodes
    return fieldNode
  }
}