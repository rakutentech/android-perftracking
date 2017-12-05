package com.rakuten.tech.mobile.perf.rewriter.classes

import com.rakuten.tech.mobile.perf.UnitSpec
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile

class ClassJarMakerSpec extends UnitSpec {

  @Rule public final TemporaryFolder projectDir = new TemporaryFolder(new File("tmp"))

  ClassJarMaker jar
  File jarTemp

  @Before void setup() {
    jarTemp = projectDir.newFile("test.jar")
    jar = new ClassJarMaker(jarTemp)
  }

  @Test void "should populate jar file with valid filepath"() {
    jar.populate(resourceFile("usertestui.jar").absolutePath)
    jar.Close()
    ClassProvider classProvider = new ClassProvider(jarTemp.absolutePath)
    assert classProvider.getClass("com.rakuten.tech.mobile.perf.core.Sender")
  }

  @Test(expected = RuntimeException.class)
  void "should fail to add duplicate entry into jar"() {
    ClassJar classJar = new ClassJar(resourceFile("usertestui.jar"))
    jar.populate(resourceFile("usertestui.jar").absolutePath)
    ArrayList<String> arrayList = classJar.getClasses()
    jar.add(arrayList.get(0), classJar)
  }

  @Test(expected = RuntimeException.class)
  void "should fail to instantiate without valid file"() {
    new ClassJarMaker(null)
  }

  @Test(expected = RuntimeException.class)
  void "should fail to populate without valid filepath"() {
    jar.populate(null)
  }

  @Test void "should add class to ClassJarMaker"() {
    jar.add("com.rakuten.test", new byte[2])
    jar.Close()
    ClassJar classJar = new ClassJar(jarTemp)
    ArrayList<String> arrayList = classJar.getClasses()
    assert arrayList.contains("com.rakuten.test")
  }

  @Test(expected = RuntimeException.class)
  void "should fail to add null entry into ClassJarMaker"() {
    jar.add(null, new byte[2])
    jar.Close()
  }
}