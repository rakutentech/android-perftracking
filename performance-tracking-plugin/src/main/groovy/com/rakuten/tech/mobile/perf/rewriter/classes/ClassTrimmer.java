package com.rakuten.tech.mobile.perf.rewriter.classes;

import java.util.ArrayList;
import java.util.List;
import org.gradle.api.logging.Logger;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;


public class ClassTrimmer {

  private final ClassProvider _provider;
  private final Logger _log;
  private final int _compileSdkVersion;

  public ClassTrimmer(String compileSdkVersion, ClassProvider provider, Logger log) {
    _provider = provider;
    _log = log;
    _compileSdkVersion = parseCompileSdkVersion(compileSdkVersion);
  }

  public ClassNode trim(ClassNode cn) {
    if ((!checkAnnotations(cn.visibleAnnotations)) || (!checkAnnotations(
        cn.invisibleAnnotations))) {
      _log.debug("Trimmed " + cn.name);
      return null;
    }

    trimMethods(cn);

    return cn;
  }

  private void trimMethods(ClassNode cn) {
    ArrayList<Object> remove = new ArrayList<Object>();

    for (Object o : cn.methods) {
      MethodNode mn = (MethodNode) o;
      if ((!checkAnnotations(mn.visibleAnnotations)) || (!checkAnnotations(
          mn.invisibleAnnotations))) {
        remove.add(mn);
      }
    }

    for (Object o : remove) {
      cn.methods.remove(o);
      _log.debug("Trimmed method " + ((MethodNode) o).name + " of " + cn.name);
    }
  }

  private boolean checkAnnotations(List<?> list) {
    if (list != null) {
      for (Object a : list) {
        if (!checkAnnotation((AnnotationNode) a)) {
          return false;
        }
      }
    }
    return true;
  }

  private boolean checkAnnotation(AnnotationNode a) {
    if (a.desc.equals("Lcom/rakuten/tech/mobile/perf/core/annotations/Exists;")) {
      if (!exists(((Type) a.values.get(1)).getClassName())) {
        return false;
      }
    } else if (a.desc
        .equals("Lcom/rakuten/tech/mobile/perf/core/annotations/MinCompileSdkVersion;")) {
      if (_compileSdkVersion < (int) a.values.get(1)) {
        return false;
      }
    } else if (a.desc
        .equals("Lcom/rakuten/tech/mobile/perf/core/annotations/MaxCompileSdkVersion;")) {
      if (_compileSdkVersion > (int) a.values.get(1)) {
        return false;
      }
    }
    return true;
  }

  private boolean exists(String name) {
    if ((name == null) || name.isEmpty()) {
      return true;
    }

    try {
      _provider.getClass(name.replace('/', '.'));
      return true;

    } catch (Throwable e) {
      return false;
    }
  }

  private int parseCompileSdkVersion(String str) {
    if ((str == null) || str.isEmpty()) {
      throw new RuntimeException("compileSdkVersion not set");
    }
    if (str.startsWith("android-")) {
      return Integer.parseInt(str.substring(8));
    }
    throw new RuntimeException("Unable to parse compileSdkVersion");
  }
}
