package com.rakuten.tech.mobile.perf.rewriter.mixins;

import java.util.ArrayList;
import java.util.HashMap;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Model of a mixin that applies a manipulation to a categories of target classes
 *
 * <p>Mixins are defined in the core project in the {@link com.rakuten.tech.mobile.perf.core.mixins}
 * package, manipulations are declared with the annotations in the
 * {@link com.rakuten.tech.mobile.perf.core.annotations} package.
 *
 * <p>To apply a mixing you should check if the mixin to a class applies using
 * {@link #match(Class)} and then manipulate it with {@link #rewrite(ClassVisitor)}.
 */
public class Mixin {

  /**
   * Name of the class defining the mixin, see {@link com.rakuten.tech.mobile.perf.core.mixins}
   */
  public String mixinClass;
  /**
   * Explicit class that the mixin targets, see
   * {@link com.rakuten.tech.mobile.perf.core.annotations.MixClass}
   */
  public String targetClass;
  /**
   * Mixin targets all subclasses of this, see
   * {@link com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf}
   */
  public String targetSubclassOf;
  /**
   * Mixin targets all implementations of this, see
   * {@link com.rakuten.tech.mobile.perf.core.annotations.MixImplementationOf}
   */
  public String targetImplementationOf;
  public final HashMap<String, MixinMethod> methods = new HashMap<String, MixinMethod>();
  public final ArrayList<MixinField> fields = new ArrayList<MixinField>();

  private final Logger _log;

  public Mixin(Logger log) {
    _log = log;
  }

  /**
   * Check if this mixin targets a given class {@code clazz}
   * @param clazz potential target of mixing
   * @return true if this mixin targets the class, false otherwise
   */
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

  /**
   * Applies mixing to a class described by a {@link ClassVisitor}.
   *
   * <p>Will manipulates methods and adds fields to the class visitor, so when you pass it to a
   * {@link org.objectweb.asm.ClassWriter} it will output the rewritten class.
   * In pseudo arithmatic: if the mixin has Δmixin_methods and Δmixin_fields then after the rewrite
   * new class will be: original class + Δmixin_methods + Δmixin_fields
   *
   * @param originalClass class writer that would output the class before mixin
   * @return new class writer that will output the original class after mixin
   */
  public ClassVisitor rewrite(final ClassVisitor originalClass) {

    return new ClassVisitor(Opcodes.ASM5, originalClass) {
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

        if ((access & (Opcodes.ACC_NATIVE | Opcodes.ACC_ABSTRACT)) > 0) {
          _log.debug("Excluding native or abstract method from rewriting " + name);
        } else {
          final MixinMethod method = methods.get(name + desc);
          if (method != null) {
            return method.rewrite(_className, originalClass, access, name, desc, signature, exceptions);
          }
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
      }

      @Override
      public void visitEnd() {
        for (MixinField f : fields) {
          f.add(originalClass);
        }
        super.visitEnd();
      }
    };
  }
}
