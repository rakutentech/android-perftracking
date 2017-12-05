package com.rakuten.tech.mobile.perf.rewriter.detours

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.Type
import org.objectweb.asm.tree.*

import static com.rakuten.tech.mobile.perf.TestUtil.*
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

public class DetourLoaderSpec extends UnitSpec {
  DetourLoader detourLoader

  @Before void setUp() {
    detourLoader = new DetourLoader(testLogger())
  }

  @Test void "should only load detours of type CallDetour"() {
    ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = jar.getClassNode("${detoursPkg}.URLDetours")

    ArrayList<Detourer> detourers = detourLoader.load(classNode)

    assert detourers.size() == 1
    assert detourers.get(0) instanceof CallDetour
  }

  @Test void "should not load detours if input classNode does not contain detour annotation"() {
    ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = jar.getClassNode("com.rakuten.tech.mobile.perf.core.mixins.ActivityMixin")

    ArrayList<Detourer> detourers = detourLoader.load(classNode)

    assert detourers.size() == 0
  }

  @Test void "should only load detours of type ParameterDetour"() {
    ClassNode classNodeStub = mock(ClassNode)
    classNodeStub.methods = [createMethodNode("testName", "(Ljava/lang/String;)V", [
        createAnnotationNode("Lcom/rakuten/tech/mobile/perf/core/annotations/DetourConstructorParameter;")
    ], null)]

    ArrayList<Detourer> detourers = detourLoader.load(classNodeStub)

    assert detourers.get(0) instanceof ParameterDetour
  }

  @Test void "should only load detours of type StaticCallDetour"() {
    ClassNode classNodeStub = mock(ClassNode)
    classNodeStub.methods = [
        createMethodNode("testName", null, [
            createAnnotationNode("Lcom/rakuten/tech/mobile/perf/core/annotations/DetourStaticCall;")],
            createInstructionList("testName"))
    ]

    ArrayList<Detourer> detourers = detourLoader.load(classNodeStub)

    assert detourers.get(0) instanceof StaticCallDetour

  }

  @Test
  void "should not load detours if methodNode name does not match with any of the Instruction Nodes"() {
    ClassNode classNodeStub = mock(ClassNode)
    classNodeStub.methods = [
        createMethodNode("testName", null, [
            createAnnotationNode("Lcom/rakuten/tech/mobile/perf/core/annotations/DetourStaticCall;")
        ], createInstructionList("testName1"))
    ]

    ArrayList<Detourer> detourers = detourLoader.load(classNodeStub)

    assert detourers.size() == 0
  }

  private static InsnList createInstructionList(def methodInstNodeName) {
    InsnList insnListStub = mock(InsnList)
    when(insnListStub.size()).thenReturn(1)
    MethodInsnNode methodInsnNodeStub = mock(MethodInsnNode)
    methodInsnNodeStub.name = methodInstNodeName
    when(insnListStub.get(0)).thenReturn(methodInsnNodeStub)
    return insnListStub
  }

  private static AnnotationNode createAnnotationNode(def annotationName) {
    AnnotationNode annotationNodeStub = mock(AnnotationNode)
    annotationNodeStub.desc = annotationName
    Type typeStub = mock(Type)
    when(typeStub.getClassName()).thenReturn("java.lang.Object")
    ArrayList<Object> objects = new ArrayList<Object>()
    objects.add("value")
    objects.add(typeStub)
    annotationNodeStub.values = objects
    return annotationNodeStub
  }

  private static MethodNode createMethodNode(
      def methodName, def methodDesc, def annotations, def instructions) {
    MethodNode methodNodeStub = mock(MethodNode)
    methodNodeStub.name = methodName
    methodNodeStub.desc = methodDesc
    methodNodeStub.visibleAnnotations = annotations
    methodNodeStub.instructions = instructions
    return methodNodeStub
  }
}