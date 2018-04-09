package com.rakuten.tech.mobile.perf.rewriter.bytecode

import groovy.transform.PackageScope

@PackageScope
interface EqualityAssert<T> {
  void isEqualTo(T other)
}