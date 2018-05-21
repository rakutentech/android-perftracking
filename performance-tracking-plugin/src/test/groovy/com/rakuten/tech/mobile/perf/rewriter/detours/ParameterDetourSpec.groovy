package com.rakuten.tech.mobile.perf.rewriter.detours

import org.junit.Before
import org.junit.Test
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static org.mockito.ArgumentMatchers.eq
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.verify

class ParameterDetourSpec {
  ParameterDetour parameterDetour

  @Before void setUp() {
    parameterDetour = new ParameterDetour(testLogger())
  }

  @Test
  void "should match if the input ownerClass or any of it's super classes is equal to the owner of the parameterDetour"() {
    parameterDetour.owner = "java.lang.Object"

    boolean match = parameterDetour.matchOwner(null, String.class)

    assert match
  }

  @Test
  void "should not match if the input ownerClass or any of it's super classes is not equal to the owner of the parameterDetour"() {
    parameterDetour.owner = "java.lang.Integer"

    boolean match = parameterDetour.matchOwner(null, String.class)

    assert !match
  }

  @Test void "should visit methodInstruction for the parameterDetour inputs"() {
    parameterDetour.detourOwner = "java.lang.String"
    parameterDetour.detourDesc = "detourDesc"
    parameterDetour.detourName = "detourName"
    MethodVisitor methodVisitorMock = mock(MethodVisitor)

    parameterDetour.rewrite(methodVisitorMock, Opcodes.NOP, "java.lang.Object", Object.class, "name", "desc", true)

    verify(methodVisitorMock).visitMethodInsn(eq(Opcodes.INVOKESTATIC), eq("java.lang.String"), eq("detourName"), eq("detourDesc"), eq(false))
    verify(methodVisitorMock).visitMethodInsn(eq(Opcodes.NOP), eq("java.lang.Object"), eq("name"), eq("desc"), eq(true))
  }
}