package com.rakuten.tech.mobile.perf.rewriter.base

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.core.base.ActivityBase
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJarMaker
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter
import org.junit.Before
import org.junit.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile
import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static org.mockito.ArgumentMatchers.any
import static org.mockito.Mockito.*

class RebaserSpec extends UnitSpec {
  Rebaser rebaser
  ClassJar jar
  ClassProvider classProvider
  ClassVisitor visitor
  Base base
  ClassReader reader

  @Before void setup() {
    jar = new ClassJar(resourceFile("usertestui.jar"))
    classProvider = new ClassProvider(resourceFile("usertestui.jar").absolutePath)
    rebaser = new Rebaser(jar, classProvider, testLogger())
    visitor = new ClassWriter(classProvider, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
    base = new BaseLoader().loadBase(
        jar.getClassNode("com.rakuten.tech.mobile.perf.core.base.ActivityBase")
    )
    reader = jar.getClassReader("com.rakuten.tech.mobile.perf.core.base.ActivityBase")
  }

  @Test void "should add the base object to the rebaser"() {
    rebaser.add(base)

    assert rebaser._bases.size() == 1
  }

  @Test void "should return same visitor object, if rebaser has a empty base list"() {
    ClassVisitor classVisitor = rebaser.rewrite(ActivityBase, visitor)

    assert classVisitor == visitor
  }

  @Test void "should return a different visitor object, if base object is added to rebaser"() {
    rebaser.add(base)

    ClassVisitor classVisitor = rebaser.rewrite(ActivityBase, visitor)

    assert classVisitor != visitor
  }

  @Test void "should create a new materialization object and assign it to the input base"() {
    Base baseMock = spy(base)
    rebaser.add(baseMock)

    visitor = rebaser.rewrite(ActivityBase, visitor)

    assert baseMock.materializations.size() == 1
  }

  @Test void "should use the existing materialization object in the base object"() {
    rebaser.add(base)
    visitor = rebaser.rewrite(ActivityBase, visitor)
    Materialization materializationMock = base.materializations.get(0)
    reader.accept(visitor, 0)

    rebaser.rewrite(ActivityBase, visitor)

    assert materializationMock == base.materializations.get(0)
  }

  @Test void "should call materialize method on materialization object in the base object"() {
    rebaser.add(base)
    visitor = rebaser.rewrite(ActivityBase, visitor)
    reader.accept(visitor, 0)
    Materialization materializationMock = spy(base.materializations.get(0))
    base.materializations.add(0, materializationMock)

    rebaser.materialize(mock(ClassJarMaker))

    verify(materializationMock).materialize(any(ClassJarMaker))
  }
}