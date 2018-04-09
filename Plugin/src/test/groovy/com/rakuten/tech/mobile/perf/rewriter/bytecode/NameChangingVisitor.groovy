package com.rakuten.tech.mobile.perf.rewriter.bytecode

import groovy.transform.PackageScope
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

@PackageScope
class NameChangingVisitor extends ClassVisitor {
  String oldClassName
  String newClassName
  String newName

  NameChangingVisitor(ClassVisitor delegate, String newName) {
    super(Opcodes.ASM5, delegate)
    this.newName = newName
  }

  @Override
  void visitSource(String source, String debug) {
    super.visitSource(null, debug)
  }

  @Override
  void visit(int version, int access, String className, String signature, String superName, String[] interfaces) {
    oldClassName = className
    newClassName = className.substring(0, className.lastIndexOf('/') + 1) + newName
    // change class name
    super.visit(version, access, newClassName, signature, superName, interfaces)
  }

  String changeName(String target) {
    target?.replace(oldClassName, newClassName)
  }

  @Override
  MethodVisitor visitMethod(int access, String methodName, String methodDesc, String methodSignature, String[] exceptions) {
    // change method calls to the classes methods
    new MethodVisitor(Opcodes.ASM5, super.visitMethod(access, methodName, methodDesc, methodSignature, exceptions)) {
      @Override
      void visitMethodInsn(int opcode, String owner, String methodInsnName, String methodInsnDesc, boolean itf) {
        if (owner == oldClassName) owner = newClassName
        super.visitMethodInsn(opcode, owner, methodInsnName, methodInsnDesc, itf)
      }

      @Override
      void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        super.visitLocalVariable(name, changeName(desc), changeName(signature), start, end, index)
      }

      @Override
      void visitFieldInsn(int opcode, String owner, String name, String desc) {
        super.visitFieldInsn(opcode, changeName(owner), name, changeName(desc))
      }
    }
  }
}