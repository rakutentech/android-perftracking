package com.rakuten.tech.mobile.perf.rewriter.detours;

import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider;
import java.util.ArrayList;
import java.util.HashMap;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class Detourer {

  private final ClassProvider _provider;

  public Detourer(ClassProvider provider) {
    _provider = provider;
  }

  @SuppressWarnings("WeakerAccess") // visible for testing
  final HashMap<String, ArrayList<Detour>> _detours = new HashMap<>();

  public void add(Detour detour) {
    String key = detour.matchMethod + detour.matchDesc;
    ArrayList<Detour> list = _detours.get(key);

    if (list == null) {
      list = new ArrayList<Detour>();
      _detours.put(key, list);
    }

    list.add(detour);
  }

  public ClassVisitor rewrite(Class<?> clazz, ClassVisitor output) {
    return new ClassVisitor(Opcodes.ASM5, output) {

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature,
          String[] exceptions) {

        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
        return new MethodVisitor(Opcodes.ASM5, mv) {

          @Override
          public void visitMethodInsn(int opcode, String owner, String name, String desc,
              boolean itf) {
            ArrayList<Detour> list = _detours.get(name + desc);
            if (list != null) {
              Class<?> ownerClass = _provider.getClass(owner.replace('/', '.'));
              for (Detour detour : list) {
                if (detour.matchOwner(owner, ownerClass)) {
                  detour.rewrite(mv, opcode, owner, ownerClass, name, desc, itf);
                  return;
                }
              }
            }
            super.visitMethodInsn(opcode, owner, name, desc, itf);
          }
        };
      }
    };
  }
}
