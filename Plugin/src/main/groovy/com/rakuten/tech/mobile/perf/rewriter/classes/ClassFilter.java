package com.rakuten.tech.mobile.perf.rewriter.classes;

import java.io.File;
import java.util.ArrayList;

public class ClassFilter {

  private final ArrayList<String> _exclude = new ArrayList<String>();

  public void exclude(String filter) {
    if (filter != null) {
      for (String pkg : filter.split(File.pathSeparator)) {
        _exclude.add(pkg.trim() + ".");
      }
    }
  }

  public boolean canRewrite(String name) {
    for (String prefix : _exclude) {
      if (name.startsWith(prefix)) {
        return false;
      }
    }
    return true;
  }
}
