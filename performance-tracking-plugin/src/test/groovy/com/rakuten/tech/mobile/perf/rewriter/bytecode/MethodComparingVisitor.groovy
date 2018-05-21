package com.rakuten.tech.mobile.perf.rewriter.bytecode

import groovy.transform.PackageScope
import org.objectweb.asm.*

@PackageScope
class MethodComparingVisitor extends MethodVisitor {
  Queue<List> visits = new LinkedList<>()
  CompareState state = CompareState.record

  MethodComparingVisitor(MethodVisitor delegate, Queue<List> visits) {
    super(Opcodes.ASM5, delegate)
    this.visits = visits
    state = CompareState.assertEquality
  }

  MethodComparingVisitor(MethodVisitor delegate) {
    super(Opcodes.ASM5, delegate)
  }

  MethodVisitor assertEqualMethod(MethodVisitor delegate) {
    return new MethodComparingVisitor(delegate, visits)
  }

  void next(Object... vals) {
    def val = vals.toList()
    if(state == CompareState.record) {
      ByteCodeUtils.log("add   method ${val}")
      visits.add(val)
    } else {
      ByteCodeUtils.log("check method ${val}")
      assert visits.remove() == val
    }
  }

  @Override
  void visitParameter(String name, int access) {
    super.visitParameter(name, access)
    next('visitParameter', name, access)
  }

  @Override
  AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    // skip for now
    return super.visitAnnotation(desc, visible)
  }

  @Override
  AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    // skip for now
    return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
  }

  @Override
  AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
    // skip for now
    return super.visitParameterAnnotation(parameter, desc, visible)
  }

  @Override
  void visitAttribute(Attribute attr) {
    // skip
    super.visitAttribute(attr)
  }

  @Override
  void visitFrame(int type, int nLocal, Object[] local, int nStack, Object[] stack) {
    // skip
    super.visitFrame(type, nLocal, local, nStack, stack)
  }

  @Override
  void visitInsn(int opcode) {
    next('visitInsn', opcode)
    super.visitInsn(opcode)
  }

  @Override
  void visitIntInsn(int opcode, int operand) {
    next('visitIntInsn', opcode, operand)
    super.visitIntInsn(opcode, operand)
  }

  @Override
  void visitVarInsn(int opcode, int var) {
    next('visitVarInsn', opcode, var)
    super.visitVarInsn(opcode, var)
  }

  @Override
  void visitTypeInsn(int opcode, String type) {
    next('visTypeInsn', opcode, type)
    super.visitTypeInsn(opcode, type)
  }

  @Override
  void visitFieldInsn(int opcode, String owner, String name, String desc) {
    next('visitFieldInsn', opcode, owner, name, desc)
    super.visitFieldInsn(opcode, owner, name, desc)
  }

  @Override
  void visitMethodInsn(int opcode, String owner, String name, String desc) {
    next('visitMethodInsn', opcode, owner, name, desc)
    super.visitMethodInsn(opcode, owner, name, desc)
  }

  @Override
  void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
    next('visitMethodInsn', opcode, owner, name, desc, itf)
    super.visitMethodInsn(opcode, owner, name, desc, itf)
  }

  @Override
  void visitInvokeDynamicInsn(String name, String desc, Handle bsm, Object... bsmArgs) {
    next('visitInvokeDynamicInsn', name, desc, bsm, bsmArgs)
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs)
  }

  @Override
  void visitJumpInsn(int opcode, Label label) {
    next('visitJumpInsn', opcode) // skip label
    super.visitJumpInsn(opcode, label)
  }

  @Override
  void visitLabel(Label label) {
    super.visitLabel(label) // skip labels
  }

  @Override
  void visitLdcInsn(Object cst) {
    next('visitLdcInsn', cst)
    super.visitLdcInsn(cst)
  }

  @Override
  void visitIincInsn(int var, int increment) {
    next('visitIincInsn', var, increment)
    super.visitIincInsn(var, increment)
  }

  @Override
  void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
    next('visitTableSwitchInsn', min, max, dflt, labels)
    super.visitTableSwitchInsn(min, max, dflt, labels)
  }

  @Override
  void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
    next('visitLookupSwitchInsn', dflt, keys, labels)
    super.visitLookupSwitchInsn(dflt, keys, labels)
  }

  @Override
  void visitMultiANewArrayInsn(String desc, int dims) {
    next('visitMultiANewArrayInsn', desc, dims)
    super.visitMultiANewArrayInsn(desc, dims)
  }

  @Override
  AnnotationVisitor visitInsnAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    // todo: annotation visitor
    return super.visitInsnAnnotation(typeRef, typePath, desc, visible)
  }

  @Override
  void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    next('visitTryCatchBlock', type) // skip labels
    super.visitTryCatchBlock(start, end, handler, type)
  }

  @Override
  AnnotationVisitor visitTryCatchAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    // todo: annotation visitor
    return super.visitTryCatchAnnotation(typeRef, typePath, desc, visible)
  }

  @Override
  void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
    next("visitLocalVariable", name, desc, signature, index) // skip labels
    super.visitLocalVariable(name, desc, signature, start, end, index)
  }

  @Override
  AnnotationVisitor visitLocalVariableAnnotation(int typeRef, TypePath typePath, Label[] start, Label[] end, int[] index, String desc, boolean visible) {
    // todo: annotation visitor
    return super.visitLocalVariableAnnotation(typeRef, typePath, start, end, index, desc, visible)
  }

  @Override
  void visitLineNumber(int line, Label start) {
    // Ignore line numbers, they don't matter fro semantic equality
    super.visitLineNumber(line, start)
  }

  @Override
  void visitMaxs(int maxStack, int maxLocals) {
    next("visitMaxs", maxStack, maxLocals)
    super.visitMaxs(maxStack, maxLocals)
  }

  @Override
  void visitEnd() {
    next("visitEnd")
    super.visitEnd()
    if(state == CompareState.record) {
      state = CompareState.assertEquality
    } else {
      assert visits.isEmpty()
    }
  }
}