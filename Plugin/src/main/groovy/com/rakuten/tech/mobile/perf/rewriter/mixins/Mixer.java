package com.rakuten.tech.mobile.perf.rewriter.mixins;

import java.util.ArrayList;
import org.objectweb.asm.ClassVisitor;

public class Mixer {

  private final ArrayList<Mixin> _mixins = new ArrayList<Mixin>();

  public void add(Mixin mixin) {
    _mixins.add(mixin);
  }

  public ClassVisitor rewrite(final Class<?> clazz, ClassVisitor output) {
    for (Mixin mixin : _mixins) {
      if (mixin.match(clazz)) {
        output = mixin.rewrite(clazz, output);
      }
    }
    return output;
  }
}
