package com.rakuten.tech.mobile.perf.rewriter.base;

import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJarMaker;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassWriter;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Materialization {

  public final Base base;
  public final int index;
  public final String name;
  public final String internalName;
  public String superName;
  public String internalSuperName;

  private final ClassProvider _provider;
  private final Logger _log;

  public Materialization(Base base, int index, ClassProvider provider, Logger log) {
    this.base = base;
    this.index = index;
    name = base.name + "_" + index;
    internalName = base.internalName + "_" + index;
    _provider = provider;
    _log = log;
  }

  public ClassVisitor rewrite(final Class<?> clazz, final ClassVisitor output) {
    _log.debug("Rebasing " + clazz.getName() + " to " + name);

    return new ClassVisitor(Opcodes.ASM5, output) {

      @Override
      public void visit(int version, int access, String name, String signature, String superName,
          String[] interfaces) {
        internalSuperName = superName; // super of modified class before instrumentation
        Materialization.this.superName = superName.replace('/', '.');
        super.visit(version, access, name, signature, internalName, interfaces);
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature,
          String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(Opcodes.ASM5, mv) {

          @Override
          public void visitMethodInsn(int opcode, String owner, String name, String desc,
              boolean itf) {
            if ((opcode == Opcodes.INVOKESPECIAL) && internalSuperName.equals(owner)) {
              owner = internalName;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
          }
        };
      }
    };
  }

  public void materialize(ClassJarMaker jarMaker) {
    _log.debug("Materializing " + name);

    final String varPrefix = "L" + base.internalName;

    ClassWriter cw = new ClassWriter(_provider,
        ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    base.cn.accept(new ClassVisitor(Opcodes.ASM5, cw) {

      @Override
      public void visit(int version, int access, String name, String signature, String superName,
          String[] interfaces) {
        super.visit(version, access, internalName, signature, internalSuperName, interfaces);
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature,
          String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(Opcodes.ASM5, mv) {

          @Override
          public void visitMethodInsn(int opcode, String owner, String name, String desc,
              boolean itf) {
            if (base.internalName.equals(owner)) {
              owner = internalName;
            } else if (opcode == Opcodes.INVOKESPECIAL && base.internalSuperName.equals(owner)) {
              owner = internalSuperName;
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
          }

          @Override
          public void visitFieldInsn(int opcode, String owner, String name, String desc) {
            if (base.internalName.equals(owner)) {
              owner = internalName;
            }
            super.visitFieldInsn(opcode, owner, name, desc);
          }

          @Override
          public void visitLocalVariable(String name, String desc, String signature, Label start,
              Label end, int index) {
            if ((desc != null) && desc.startsWith(varPrefix)) {
              desc = "L" + internalName + desc.substring(varPrefix.length());
            }
            if ((signature != null) && signature.startsWith(varPrefix)) {
              signature = "L" + internalName + signature.substring(varPrefix.length());
            }
            super.visitLocalVariable(name, desc, signature, start, end, index);
          }
        };
      }
    });

    jarMaker.add(name, cw.toByteArray());
  }
}

