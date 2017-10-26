package com.rakuten.tech.mobile.perf.rewriter;


import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SUPER;
import static org.objectweb.asm.Opcodes.V1_6;

import org.objectweb.asm.ClassWriter;

/**
 * Utility class to generate class files dynamically using ASM library.
 */
public class ClassGenerator {

  /**
   * Creates AppPerformanceConfig.class file with a static boolean field AppPerformanceConfig#enabled in it.
   *
   * @param value Initializes the AppPerformanceConfig#enabled with value.
   * @return byte array of the class.
   */
  public static byte[] generateConfigClass(boolean value) {

    ClassWriter cw = new ClassWriter(0);
    cw.visit(V1_6, ACC_FINAL + ACC_SUPER,
        "com/rakuten/tech/mobile/perf/runtime/internal/AppPerformanceConfig", null,
        "java/lang/Object", null);
    cw.visitField(ACC_PUBLIC + ACC_STATIC, "enabled", "Z", null, value);
    cw.visitEnd();
    return cw.toByteArray();
  }
}