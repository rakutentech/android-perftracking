package com.rakuten.tech.mobile.perf.rewriter.classes

import com.rakuten.tech.mobile.perf.UnitSpec
import org.junit.Before
import org.junit.Test

import static com.rakuten.tech.mobile.perf.TestUtil.resourceFile

public class ClassWriterSpec extends UnitSpec {

  ClassWriter classWriter

  @Before def void setup() {
    classWriter = new ClassWriter(new ClassProvider(resourceFile("usertestui.jar").absolutePath),
        ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
  }

  @Test def void "should provide common super class for given sub classes"() {
    String type1 = "jp.co.rakuten.api.rae.engine.TokenRequest"
    String type2 = "jp.co.rakuten.api.rae.engine.TokenCancelRequest"
    assert classWriter.getCommonSuperClass(type1, type2) == "jp/co/rakuten/api/rae/engine/EngineBaseRequest"
  }

  @Test def void "should provide object class as super class when any one input parameter is interface"() {
    String type1 = "jp.co.rakuten.api.rae.engine.TokenRequest"
    String type2 = "jp.co.rakuten.api.core.TokenableRequest"
    assert classWriter.getCommonSuperClass(type1, type2) == "java/lang/Object"
  }

  @Test def void "should provide same super type even by swapping input parameter"() {
    String type1 = "jp.co.rakuten.api.rae.engine.EngineBaseRequest"
    String type2 = "jp.co.rakuten.api.rae.engine.TokenRequest"
    def super12 = classWriter.getCommonSuperClass(type1, type2)
    def super21 = classWriter.getCommonSuperClass(type2, type1)
    assert super12 == super21
  }

  @Test(expected = RuntimeException.class)
  def void "should fail to get common super type when given class not found"() {
    String type1 = "test"
    String type2 = "test"
    classWriter.getCommonSuperClass(type1, type2)
  }

}