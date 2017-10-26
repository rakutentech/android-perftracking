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

public class StaticCallDetourSpec extends UnitSpec {
  StaticCallDetour staticCallDetour

  @Before void setUp() {
    staticCallDetour = new StaticCallDetour(testLogger())
  }

  @Test void "should match if the input owner is equal to the owner of the staticCallDetour"() {
    staticCallDetour.owner = "java.lang.Object"

    boolean match = staticCallDetour.matchOwner("java.lang.Object", null)

    assert match
  }

  @Test void "should not match if the input owner is not equal to the owner of the callDetour"() {
    staticCallDetour.owner = "java.lang.class"

    boolean match = staticCallDetour.matchOwner("java.lang.Object", null)

    assert !match
  }

  @Test void "should visit method instance for the staticCallDetour inputs"() {
    staticCallDetour.detourOwner = "java.lang.String"
    MethodVisitor methodVisitorMock = mock(MethodVisitor)

    staticCallDetour.rewrite(methodVisitorMock, Opcodes.NOP, "java.lang.Object", Object.class, "name", "desc", true)

    verify(methodVisitorMock).visitMethodInsn(eq(Opcodes.INVOKESTATIC), eq("java.lang.String"), eq("name"), eq("desc"), eq(false))
  }
}