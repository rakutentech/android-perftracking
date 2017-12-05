package com.rakuten.tech.mobile.perf.rewriter.detours

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode

import static com.rakuten.tech.mobile.perf.TestUtil.*
import static org.mockito.ArgumentMatchers.any
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.*

public class DetourerSpec extends UnitSpec {
  Detourer detourer

  @Before void setUp() {
    ClassProvider classProviderMock = mock(ClassProvider)
    detourer = new Detourer(classProviderMock)
  }

  @Test
  void "should add the input detour to the Detourer collection, should create an ArrayList and add the input into the collection"() {
    Detour detourStub = mock(Detour)
    detourStub.matchMethod = "matchMethod"
    detourStub.matchDesc = "matchDesc"

    detourer.add(detourStub)

    assert detourer._detours.get("matchMethod" + "matchDesc").size() == 1
  }

  @Test
  void "should add the input detourers into the collection, should add new entry to the arraylist if matches"() {
    Detour detourStub = mock(Detour)
    detourStub.matchMethod = "matchMethod"
    detourStub.matchDesc = "matchDesc"

    detourer.add(detourStub)
    detourer.add(detourStub)

    assert detourer._detours.get("matchMethod" + "matchDesc").size() == 2
  }

  @Test
  void "should rewrite method of URL with URLDetours, if URL class is available in ClassProvider"() {
    DetourLoader detourLoader = new DetourLoader(testLogger())
    ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
    ClassNode classNode = jar.getClassNode("${detoursPkg}.URLDetours")
    ArrayList<Detourer> detourers = detourLoader.load(classNode)
    ClassProvider classProvider = new ClassProvider(resourceFile("usertestui.jar").absolutePath)
    detourer.add(detourers.get(0))
    ClassVisitor visitor = new ClassWriter(classProvider, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
    ClassReader reader = jar.getClassReader("${detoursPkg}.URLDetours")
    ClassVisitor classVisitorMock = spy(detourer.rewrite(Object.class, visitor))

    reader.accept(classVisitorMock, 0)

    verify(classVisitorMock).visitMethod(eq(Opcodes.LCONST_0), eq("openConnection"), eq("(Ljava/net/URL;)Ljava/net/URLConnection;"), eq(null), any())
  }
}