package com.rakuten.tech.mobile.perf.rewriter.mixins

import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.tree.FieldNode

import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static org.mockito.Mockito.*

class MixinFieldSpec {
  MixinField mixinField

  @Before
  void setup() {
    FieldNode fieldNode = new FieldNode(0, "test_name", "test_desc", "test_signature", new Integer(1))
    mixinField = new MixinField(testLogger(), fieldNode)
  }

  @Test
  void "should visit the field, when adding a visitor to the mixin field"() {
    ClassVisitor classVisitorMock = mock(ClassVisitor)
    when(classVisitorMock.visitField(anyInt(), anyString(),
        anyString(), anyString(), any(Object))).thenReturn(null)

    mixinField.add(classVisitorMock)

    verify(classVisitorMock).visitField(anyInt(),
        anyString(), anyString(), anyString(), any(Object))
  }

}