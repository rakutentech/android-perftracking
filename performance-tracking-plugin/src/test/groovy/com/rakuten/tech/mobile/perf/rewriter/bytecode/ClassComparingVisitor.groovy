package com.rakuten.tech.mobile.perf.rewriter.bytecode

import groovy.transform.PackageScope
import org.objectweb.asm.*

@PackageScope
class ClassComparingVisitor extends ClassVisitor {
  Map<String, List> visits = new HashMap<>()
  CompareState state = CompareState.record

  ClassComparingVisitor() {
    super(Opcodes.ASM5)
  }

  void assertEqual(byte[] expected, byte[] actual) {
    new ClassReader(expected).accept(this, 0)
    state = CompareState.assertEquality
    new ClassReader(actual).accept(this, 0)
  }

  void next(Object... vals) {
    def key = vals[0]
    def val = vals.toList()
    if(state == CompareState.record) {
      ByteCodeUtils.log("add   class ${val}")
      visits.put(key, val)
    } else {
      ByteCodeUtils.log("check class ${val}")
      assert visits.remove(key) == val
    }
  }

  MethodVisitor nextMethod(String key, MethodVisitor delegate) {
    if(state == CompareState.record) {
      ByteCodeUtils.log("add   class ${key}")
      visits.put(key, new MethodComparingVisitor(delegate))
      visits.get(key)
    } else {
      ByteCodeUtils.log("check class ${key}")
      (visits.remove(key) as MethodComparingVisitor).assertEqualMethod(delegate)
    }
  }

  @Override
  void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    super.visit(version, access, name, signature, superName, interfaces)
    next('visit-' + name, version, access, name, signature, superName, interfaces)
  }

  @Override
  void visitSource(String source, String debug) {
    super.visitSource(source, debug)
    next('visitSource', source, debug)
  }

  @Override
  void visitOuterClass(String owner, String name, String desc) {
    super.visitOuterClass(owner, name, desc)
    next('visitOuterClass-' + name, owner, name, desc)
  }

  @Override
  AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    // TODO: annotation visitor
    return super.visitAnnotation(desc, visible)
  }

  @Override
  AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath, String desc, boolean visible) {
    // TODO: annotation visitor
    return super.visitTypeAnnotation(typeRef, typePath, desc, visible)
  }

  @Override
  void visitAttribute(Attribute attr) {
    // skip for now
    super.visitAttribute(attr)
  }

  @Override
  void visitInnerClass(String name, String outerName, String innerName, int access) {
    // skip for now
    super.visitInnerClass(name, outerName, innerName, access)
  }

  @Override
  FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
    next('visitField-'+name, access, name, desc, signature, value)
    return super.visitField(access, name, desc, signature, value) // todo: field visitor
  }

  @Override
  MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    return nextMethod("visitMethod-$name$desc", super.visitMethod(access, name, desc,
        signature, exceptions))
  }

  @Override
  void visitEnd() {
    super.visitEnd()
    if(state == CompareState.assertEquality) assert visits.isEmpty()
  }
}