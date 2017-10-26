package com.rakuten.tech.mobile.perf.rewriter.base;

import org.objectweb.asm.tree.ClassNode;

public class BaseLoader {

  public Base loadBase(ClassNode cn) {
    Base base = new Base();
    base.name = cn.name.replace('/', '.');
    base.internalName = cn.name;
    base.superName = cn.superName.replace('/', '.');
    base.internalSuperName = cn.superName;
    base.cn = cn;
    return base;
  }
}
