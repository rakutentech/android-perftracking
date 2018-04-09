package com.rakuten.tech.mobile.perf.rewriter.bytecode

import org.objectweb.asm.*

class ByteCodeUtils {

  /**
   * Checks for semantic bytecode equality. It ignores labels in bytecode, because they are
   * inherently changing with every compilation/instrumentation.
   * Relies on {@link ClassComparingVisitor} and shares all it's characteristics, namely
   * <ul>
   *   <li>ignores annotations (limitation)</li>
   *   <li>ignores field initializations (limitation)</li>
   *   <li>ignores labels</li>
   *   <li>ignores declaration order of class members</li>
   *   <li>ignores line numbers</li>
   * </ul>
   * @param expected
   * @return
   */
  static EqualityAssert<byte[]> assertThatBytecode(byte[] expected) {
    { actual -> new ClassComparingVisitor().assertEqual(expected, actual)} as
        EqualityAssert<byte[]>
  }

  /**
   * Load the byte code for the class named {@code name} and {@code name} + "Post" from java test
   * package {@link com.rakuten.tech.mobile.perf.test}. The byte code of both classes will use the
   * name {@code name}, so the expected byte code should be (semantically) equal to the original
   * bytecode + instrumentation.
   * @param name Class name
   * @return byte code test data with original and expected bytecode & class reader
   */
  static ByteCodeTestData testDataBytecode(String name) {
    new ByteCodeTestData(name)
  }

  /**
   * Reads the byte code from a reader and will strip the "compiled from" info
   *
   * @param src class to be read
   * @param mixins optional mixins to apply while reading the class
   * @return resulting bytecode with mixins
   */
  static byte[] readClassByteCode(ClassReader src, Closure<ClassVisitor>... mixins) {
    def writer = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES)
    ClassVisitor visitor = writer
    for (Closure<ClassVisitor> mixin : [ByteCodeUtils.&stripSouce, *mixins]) {
      visitor = mixin(visitor)
    }
    src.accept(visitor, 0)

    writer.toByteArray()
  }

  static ClassVisitor stripPackage(ClassVisitor delegate) {
    new ClassVisitor(Opcodes.ASM5, delegate) {
      @Override
      void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        def newName = name.substring(name.lastIndexOf('/') + 1)
        super.visit(version, access, newName, signature, superName, interfaces)
      }
    }
  }

  static ClassVisitor stripSouce(ClassVisitor delegate) {
    new ClassVisitor(Opcodes.ASM5, delegate) {
      @Override
      void visitSource(String source, String debug) {
        super.visitSource(null, debug)
      }
    }
  }

  static ClassVisitor replaceName(String newName, ClassVisitor delegate) {
    new NameChangingVisitor(delegate, newName)
  }

  static File writeClassToFile(String className, byte[] byteCode) {
    def outDir = new File('testOutput')
    if(!outDir.exists()) outDir.mkdir()
    def fileName = className.endsWith('.class') ? className : "${className}.class"
    fileName = fileName.substring(fileName.lastIndexOf('/') + 1)
    def classFile = new File(outDir, fileName)
    if(classFile.exists()) classFile.delete()
    classFile.createNewFile()
    classFile << byteCode
    classFile
  }

  static InputStream loadTestDataClassAsStream(String className) {
    this.classLoader.getResourceAsStream(
        "com/rakuten/tech/mobile/perf/testdata/${className.replace ('.', '/')}.class")
  }

  static ClassReader readClass(String className) {
    new ClassReader(loadTestDataClassAsStream(className))
  }

  static void log(String message, Object... args) {
    // uncomment for detailed logging of byte code comparison
    System.out.println(String.format(message, args))
  }
}
