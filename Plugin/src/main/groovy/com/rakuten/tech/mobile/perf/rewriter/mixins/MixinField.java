package com.rakuten.tech.mobile.perf.rewriter.mixins;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.tree.FieldNode;

public class MixinField {

  private final Logger _log;
  private final FieldNode _fn;
  public final String name;

  public MixinField(Logger log, FieldNode fn) {
    _log = log;
    _fn = fn;
    name = fn.name;
  }

  public void add(ClassVisitor cv) {
    _log.info("Adding field " + _fn.name);
    cv.visitField(_fn.access, _fn.name, _fn.desc, _fn.signature, _fn.value);
    //_fn.accept(cv);
  }
}