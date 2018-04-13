package com.rakuten.tech.mobile.perf.rewriter.classes

import org.junit.Before
import org.junit.Test

class ClassFilterSpec {

  ClassFilter classFilter

  @Before
  void setup() {
    classFilter = new ClassFilter()
  }

  @Test
  void "should exclude all classes from package, but still accept classes from other packages"() {
    classFilter.exclude("package.to.exclude")

    assert !classFilter.canRewrite("package.to.exclude.SomeClass")
    assert  classFilter.canRewrite("other.package.OtherClass")
  }

  @Test
  void "should accept all classes when we exclude null packages"() {
    classFilter.exclude(null)

    assert classFilter.canRewrite("package.any.nonexclude.MyClass")
  }

  @Test
  void "should exclude classes from all excluded packages"() {
    classFilter.exclude("package.to.exclude1")
    classFilter.exclude("package.to.exclude2")

    assert  classFilter.canRewrite("package.any.nonexclude.SisClass")
    assert !classFilter.canRewrite("package.to.exclude1.FathClass")
    assert !classFilter.canRewrite("package.to.exclude2.BroClass")
  }

  @Test
  void "should pass all exclude package names as single param, check if all classes are exluded from those packages"() {
    classFilter.exclude("package.to.exclude1" + File.pathSeparator + "package.to.exclude2")

    assert  classFilter.canRewrite("package.any.nonexclude.SisClass")
    assert !classFilter.canRewrite("package.to.exclude1.FathClass")
    assert !classFilter.canRewrite("package.to.exclude2.BroClass")
  }
}