package com.rakuten.tech.mobile.perf.rewriter.mixins;

import java.util.ArrayList;
import java.util.HashMap;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;


public class Mixin {

  public String mixinClass;
  public String targetClass;
  public String targetSubclassOf;
  public String targetImplementationOf;
  public final HashMap<String, MixinMethod> methods = new HashMap<String, MixinMethod>();
  public final ArrayList<MixinField> fields = new ArrayList<MixinField>();

  private final Logger _log;

  public Mixin(Logger log) {
    _log = log;
  }

  public boolean match(Class<?> clazz) {
    if (targetClass != null) {
      return targetClass.equals(clazz.getName());
    }
    if (targetSubclassOf != null) {
      for (Class<?> s = clazz.getSuperclass(); s != null; s = s.getSuperclass()) {
        if (targetSubclassOf.equals(s.getName())) {
          return true;
        }
      }
      return false;
    }
    if (targetImplementationOf != null) {
      for (Class<?> i : clazz.getInterfaces()) {
        if (targetImplementationOf.equals(i.getName())) {
          return true;
        }
      }
      return false;
    }
    return false;
  }

  public ClassVisitor rewrite(final Class<?> clazz, final ClassVisitor output) {
    _log.debug("Mixing " + clazz.getName());

    return new ClassVisitor(Opcodes.ASM5, output) {
      private String _className;

      @Override
      public void visit(int version, int access, String name, String signature, String superName,
          String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        _className = name;
      }

      @Override
      public MethodVisitor visitMethod(int access, String name, String desc, String signature,
          String[] exceptions) {
        if ((access & Opcodes.ACC_NATIVE) == 0) {
          final MixinMethod method = methods.get(name + desc);
          if (method != null) {
            return method.rewrite(_className, output, access, name, desc, signature, exceptions);
          }
        } else {
          _log.debug("Native method excluded from rewriting " + name);
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
      }

      @Override
      public void visitEnd() {
        for (MixinField f : fields) {
          f.add(output);
        }
        super.visitEnd();
      }
    };
  }
}
