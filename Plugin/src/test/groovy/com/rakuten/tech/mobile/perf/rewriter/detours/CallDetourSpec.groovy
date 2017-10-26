package com.rakuten.tech.mobile.perf.rewriter.detours

import com.rakuten.tech.mobile.perf.UnitSpec
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

public class CallDetourSpec extends UnitSpec {
  CallDetour callDetour

  @Before void setUp() {
    callDetour = new CallDetour(testLogger())
  }

  @Test
  void "should match if the input ownerClass or any of it's super classes is equal to the owner of the callDetour"() {
    callDetour.owner = "java.lang.Object"

    boolean match = callDetour.matchOwner(null, String.class)

    assert match
  }

  @Test
  void "should not match if the input ownerClass or any of it's super classes is not equal to the owner of the callDetour"() {
    callDetour.owner = "java.lang.Integer"

    boolean match = callDetour.matchOwner(null, String.class)

    assert !match
  }

  @Test
  void "should visit methodInstruction for the callDetour inputs"() {
    callDetour.detourOwner = "com.rakuten.tech.mobile.perf.core.detours.URLDetours"
    callDetour.detourDesc = "detourDesc"
    MethodVisitor methodVisitorMock = mock(MethodVisitor)

    callDetour.rewrite(methodVisitorMock, Opcodes.NOP, "java.net.URL", Object.class, "name", "desc", true)

    verify(methodVisitorMock).visitMethodInsn(eq(Opcodes.INVOKESTATIC), eq("com.rakuten.tech.mobile.perf.core.detours.URLDetours"), eq("name"), eq("detourDesc"), eq(false))
  }
}