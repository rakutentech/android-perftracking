package com.rakuten.tech.mobile.perf.rewriter.bytecode

import groovy.transform.PackageScope

@PackageScope
enum CompareState {
  record, assertEquality
}