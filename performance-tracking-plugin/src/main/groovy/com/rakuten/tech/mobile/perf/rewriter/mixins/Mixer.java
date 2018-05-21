package com.rakuten.tech.mobile.perf.rewriter.mixins;

import java.util.ArrayList;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.ClassVisitor;

public class Mixer {

  private final ArrayList<Mixin> _mixins = new ArrayList<>();
  private final Logger _log;

  public Mixer(Logger log) {
    _log = log;
  }

  public void add(Mixin mixin) {
    _mixins.add(mixin);
  }

  public ClassVisitor rewrite(final Class<?> clazz, ClassVisitor originalClass) {
    ClassVisitor mixedClass = originalClass;
    for (Mixin mixin : _mixins) {
      if (mixin.match(clazz)) {
        _log.debug("Mixing " + clazz.getName());
        mixedClass = mixin.rewrite(mixedClass);
      }
    }
    return mixedClass;
  }
}
