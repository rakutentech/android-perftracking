package com.rakuten.tech.mobile.perf.rewriter.classes;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.util.ArrayList;

public class ClassProvider {

  private final URLClassLoader _classLoader;

  public ClassProvider(String classpath) {
    try {
      ArrayList<URL> urls = new ArrayList<URL>();
      for (String path : classpath.split(File.pathSeparator)) {
        urls.add(Paths.get(path).toFile().toURI().toURL());
      }
      _classLoader = new URLClassLoader(urls.toArray(new URL[0]), getClass().getClassLoader());
    } catch (Exception e) {
      throw new RuntimeException("Failed to create class loader");
    }
  }

  public Class<?> getClass(String name) {
    try {
      return _classLoader.loadClass(name);

    } catch (Exception e) {
      throw new RuntimeException("Failed to load class " + name, e);
    }
  }
}
