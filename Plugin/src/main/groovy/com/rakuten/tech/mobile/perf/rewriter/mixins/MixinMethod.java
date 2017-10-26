package com.rakuten.tech.mobile.perf.rewriter.mixins;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.MethodNode;

public class MixinMethod {

  public static String PREFIX = "com_rakuten_tech_mobile_perf_";

  private final Mixin _mixin;
  private final MethodNode _mn;
  private final Logger _log;

  public MixinMethod(Mixin mixin, MethodNode mn, Logger log) {
    _mixin = mixin;
    _mn = mn;
    _log = log;
  }

  public MethodVisitor rewrite(final String className, ClassVisitor cv, int access, String name,
      String desc, String signature, String[] exceptions) {
    _log.debug("Mixing method " + className + "." + name + desc);

    final String varPrefix = "L" + _mixin.mixinClass;

    MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);
    _mn.accept(new MethodVisitor(Opcodes.ASM5, mv) {

      @Override
      public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (_mixin.mixinClass.equals(owner)) {
          owner = className;
          if (name.equals(_mn.name)) {
            opcode = Opcodes.INVOKESPECIAL;
            name = PREFIX + name;
          }
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (_mixin.mixinClass.equals(owner)) {
          owner = className;
        }
        super.visitFieldInsn(opcode, owner, name, desc);
      }

      @Override
      public void visitLocalVariable(String name, String desc, String signature, Label start,
          Label end, int index) {
        if ((desc != null) && desc.startsWith(varPrefix)) {
          desc = "L" + className + desc.substring(varPrefix.length());
        }
        if ((signature != null) && signature.startsWith(varPrefix)) {
          signature = "L" + className + signature.substring(varPrefix.length());
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
      }

    });

    return cv.visitMethod(Opcodes.ACC_PRIVATE, PREFIX + name, desc, signature, exceptions);
  }
}
