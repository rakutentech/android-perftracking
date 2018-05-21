package com.rakuten.tech.mobile.perf.rewriter.detours;

import org.gradle.api.logging.Logger;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ParameterDetour extends Detour {

  private final Logger _log;

  public String owner;
  public String detourOwner;
  public String detourName;
  public String detourDesc;

  public ParameterDetour(Logger log) {
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
    _log.info("Detouring " + owner + "." + name + desc);
    mv.visitMethodInsn(Opcodes.INVOKESTATIC, detourOwner, detourName, detourDesc, false);
    mv.visitMethodInsn(opcode, owner, name, desc, itf);
  }
}
