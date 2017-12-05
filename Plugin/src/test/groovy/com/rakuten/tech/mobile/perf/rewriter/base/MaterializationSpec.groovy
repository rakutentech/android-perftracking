package com.rakuten.tech.mobile.perf.rewriter.base

import com.rakuten.tech.mobile.perf.UnitSpec
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJarMaker
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.objectweb.asm.ClassReader

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile
import static com.rakuten.tech.mobile.perf.TestUtil.testLogger
import static org.mockito.Mockito.spy

class MaterializationSpec extends UnitSpec {
  @Rule public final TemporaryFolder tempDir = new TemporaryFolder()
  def index = 1
  File jar
  Materialization materialization

  Class clazz
  ClassWriter writer
  ClassReader reader
  Base base
  ClassProvider provider

  String targetClass = "com.rakuten.sample.MainActivity"

  @Before void setup() {
    ClassJar classJar = new ClassJar(resourceFile("usertestui.jar"))
    base = new BaseLoader().loadBase(
        classJar.getClassNode("com.rakuten.tech.mobile.perf.core.base.ActivityBase")
    )
    def classpath = resourceFile("usertestui.jar").absolutePath + File.pathSeparator +
        resourceFile("android23.jar").absolutePath + File.pathSeparator +
        resourceFile("SampleApp.jar")
    provider = new ClassProvider(classpath);

    clazz = provider.getClass(targetClass)
    reader = new ClassJar(resourceFile("SampleApp.jar")).getClassReader(targetClass)
    writer = new ClassWriter(provider, ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

    materialization = new Materialization(base, index++, provider, testLogger());
    jar = resourceFile("usertestui.jar");
  }

  @Test void "should insert super class in inheritance hierarchy"() {
    def visitor = materialization.rewrite(clazz, writer)
    def outputJar = tempDir.newFile('out.jar')
    ClassJarMaker outputMaker = new ClassJarMaker(outputJar)
    reader.accept(visitor, 0)

    materialization.materialize(outputMaker)

    outputMaker.add(targetClass, writer.toByteArray())
    outputMaker.Close()
    def rewrittenClass = new ClassProvider(outputJar.absolutePath).getClass(targetClass)
    assert materialization.internalSuperName == "android/app/Activity"
    assert rewrittenClass.getSuperclass().name.replace(".", "/") == materialization.internalName
  }

  @Test void "should materialize and add the class to ClassJarMaker"() {
    ClassJar jar = new ClassJar(resourceFile("usertestui.jar"))
    Base baseStub = spy(new BaseLoader().loadBase(
        jar.getClassNode("com.rakuten.tech.mobile.perf.core.base.WebViewClientBase"))
    )
    baseStub.internalName = "android/webkit/WebViewClient"
    materialization = new Materialization(baseStub, index++, provider, testLogger());
    File tempJarFile = tempDir.newFile("temp.jar")
    ClassJarMaker classJarMaker = new ClassJarMaker(tempJarFile)

    materialization.materialize(classJarMaker)

    classJarMaker.Close()
    assert new ClassJar(tempJarFile).hasClass(materialization.name)
  }
}