package com.rakuten.tech.mobile.perf.rewriter.detours;

import org.objectweb.asm.MethodVisitor;

public abstract class Detour {

  public String matchMethod;
  public String matchDesc;

  public abstract boolean matchOwner(String owner, Class<?> ownerClass);

  public abstract void rewrite(MethodVisitor mv, int opcode, String owner, Class<?> ownerClass,
      String name, String desc, boolean itf);
}
