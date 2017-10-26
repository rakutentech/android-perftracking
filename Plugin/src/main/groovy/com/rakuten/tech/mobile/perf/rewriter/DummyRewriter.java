package com.rakuten.tech.mobile.perf.rewriter;

import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJar;
import com.rakuten.tech.mobile.perf.rewriter.classes.ClassJarMaker;
import java.io.File;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

/**
 * Dummy ReWriter to exclude performance tracking from debug build.
 */

public class DummyRewriter implements Rewriter {

  public String input;
  public String outputJar;
  public String tempJar;
  public String classpath;
  public String exclude;
  public String compileSdkVersion;
  public final Logger _log;

  public DummyRewriter() {
    _log = Logging.getLogger(DummyRewriter.class.getSimpleName());
  }

  public void rewrite() {
    _log.debug(input);
    _log.debug("DummyRewriter Populating temp JAR");
    ClassJarMaker tempMaker = new ClassJarMaker(new File(tempJar));
    try {
      tempMaker.populate(input);
    } finally {
      tempMaker.Close();
    }

    ClassJar temp = new ClassJar(new File(tempJar));
    ClassJarMaker outputMaker = new ClassJarMaker(new File(outputJar));
    try {
      _log.info("DummyRewriter classes of : " + temp.getJarFile().getName());
      for (String name : temp.getClasses()) {
        if (name.equals("com.rakuten.tech.mobile.perf.runtime.internal.AppPerformanceConfig")) {
          _log.debug("Modifying " + name + " dynamically with enabled = false");
          outputMaker.add(name, ClassGenerator.generateConfigClass(false));
        } else {
          _log.debug("Adding class with no rewriting: " + name);
          outputMaker.add(name, temp);
        }
      }
    } finally {
      outputMaker.Close();
    }
  }
}