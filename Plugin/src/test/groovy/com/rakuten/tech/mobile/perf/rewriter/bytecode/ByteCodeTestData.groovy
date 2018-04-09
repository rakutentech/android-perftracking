package com.rakuten.tech.mobile.perf.rewriter.bytecode

import org.objectweb.asm.ClassReader

class ByteCodeTestData {
  String className
  ClassReader originalClass
  ClassReader expectedClass
  byte[] originalByteCode
  byte[] expectedByteCode
  def classFiles = []

  ByteCodeTestData(String name) {
    this.className = name.substring(name.lastIndexOf('/') + 1)
    def pathToClass = name
    originalClass = ByteCodeUtils.readClass("${pathToClass}")
    expectedClass = ByteCodeUtils.readClass("${pathToClass}Post")
    originalByteCode = ByteCodeUtils.readClassByteCode(originalClass)

    expectedByteCode = ByteCodeUtils.readClassByteCode(expectedClass, ByteCodeUtils.&replaceName.curry(className))
  }

  /**
   * writes the the byte code of this test data set plus the actual byte code after
   * instrumentation to {@code $projectDir/testOutput} for manual inspection.
   * Use {@link #clear} to delete all recorded files.
   * @param actualByteCode byte code after inspection
   */
  void record(byte[] actualByteCode) {
    // write classes in case they mismatch, for manual inspection
    classFiles << ByteCodeUtils.writeClassToFile("${className}", originalByteCode)
    classFiles << ByteCodeUtils.writeClassToFile("${className}Post", expectedByteCode)
    classFiles << ByteCodeUtils.writeClassToFile("${className}Actual", actualByteCode)
  }

  /**
   * Deletes all recorded test files of this test data set, including the actual byte code.
   * Does nothing if {@link #record} wasn't called before this.
   */
  void clear() {
    classFiles.forEach { it.delete() }
  }
}