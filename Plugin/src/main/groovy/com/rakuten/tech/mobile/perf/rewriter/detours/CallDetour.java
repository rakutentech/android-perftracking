package com.rakuten.tech.mobile.perf.rewriter.detours;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class CallDetour extends Detour {

  private final Logger _log;

  public String owner;
  public String detourOwner;
  public String detourDesc;

  public CallDetour(Logger log) {
    _log = log;
  }

  @Override
  public boolean matchOwner(String owner, Class<?> ownerClass) {
    for (Class<?> c = ownerClass; c != null; c = c.getSuperclass()) {
      if (this.owner.equals(c.getName())) {
        return true;
      }
    }
    return false;
  }

  @Override
  public void rewrite(MethodVisitor mv, int opcode, String owner, Class<?> ownerClass, String name,
      String desc, boolean itf) {
    _log.debug("Detouring " + owner + "." + name + desc);
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, detourOwner, name, detourDesc, false);
  }
}
