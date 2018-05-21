package com.rakuten.tech.mobile.perf.rewriter.base;

import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJarMaker;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassProvider;
import java.util.HashMap;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;

public class Rebaser {

  private final ClassJar _jar;
  @SuppressWarnings("WeakerAccess") // visible for testing
  final HashMap<String, Base> _bases = new HashMap<String, Base>();
  private final ClassProvider _provider;
  private final Logger _log;

  public Rebaser(ClassJar jar, ClassProvider provider, Logger log) {
    _jar = jar;
    _provider = provider;
    _log = log;
  }

  public void add(Base base) {
    _bases.put(base.superName, base);
  }

  public ClassVisitor rewrite(Class<?> clazz, ClassVisitor output) {
    Base base = null;
    Class<?> s = clazz.getSuperclass();

    if (s != null) {
      base = _bases.get(s.getName());
      if (base == null) {
        if (!_jar.hasClass(s.getName())) {
          for (s = s.getSuperclass(); s != null; s = s.getSuperclass()) {
            base = _bases.get(s.getName());
            if (base != null) {
              break;
            }
          }
        }
      }
    }

    if (base == null) {
      return output;
    }

    Materialization materialization = null;

    for (Materialization m : base.materializations) {
      if (m.superName.equals(clazz.getSuperclass().getName())) {
        materialization = m;
        break;
      }
    }

    if (materialization == null) {
      materialization = new Materialization(base, base.materializations.size() + 1, _provider,
          _log);
      base.materializations.add(materialization);
    }

    return materialization.rewrite(clazz, output);
  }

  public void materialize(ClassJarMaker jarMaker) {
    for (Base base : _bases.values()) {
      for (Materialization m : base.materializations) {
        m.materialize(jarMaker);
      }
    }
  }
}

