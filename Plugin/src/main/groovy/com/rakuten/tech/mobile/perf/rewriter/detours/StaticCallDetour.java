package com.rakuten.tech.mobile.perf.rewriter.detours;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class StaticCallDetour extends Detour {

  private final Logger _log;

  public String owner;
  public String detourOwner;

  public StaticCallDetour(Logger log) {
    _log = log;
  }

  @Override
  public boolean matchOwner(String owner, Class<?> ownerClass) {
    return this.owner.equals(owner);
  }

  @Override
  public void rewrite(MethodVisitor mv, int opcode, String owner, Class<?> ownerClass, String name,
      String desc, boolean itf) {
    _log.info("Detouring " + owner + "." + name + desc);
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, detourOwner, name, desc, false);
  }
}
