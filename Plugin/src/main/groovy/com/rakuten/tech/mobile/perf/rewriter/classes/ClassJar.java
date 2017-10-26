package com.rakuten.tech.mobile.perf.rewriter.classes;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

public class ClassJar {

  private final JarFile _jar;
  private final HashMap<String, JarEntry> _entries = new HashMap<String, JarEntry>();
  private final ArrayList<String> _classes = new ArrayList<String>();

  public ClassJar(File file) {
    try {
      _jar = new JarFile(file);
      Enumeration<JarEntry> entries = _jar.entries();
      while (entries.hasMoreElements()) {
        JarEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.toLowerCase().endsWith(".class")) {
          name = name.substring(0, name.length() - ".class".length()).replace('/', '.');
          _classes.add(name);
          _entries.put(name, entry);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to open jar file", e);
    }
  }

  public ArrayList<String> getClasses() {
    return _classes;
  }

  public JarFile getJarFile() {
    return _jar;
  }

  InputStream getInputStream(String name) {
    try {
      JarEntry entry = _entries.get(name);
      return _jar.getInputStream(entry);
    } catch (Exception e) {
      throw new RuntimeException("Failed to get input stream", e);
    }
  }

  public ClassReader getClassReader(String name) {
    try {
      return new ClassReader(getInputStream(name));
    } catch (Exception e) {
      throw new RuntimeException("Failed to create class reader", e);
    }
  }

  public ClassNode getClassNode(String name) {
    ClassNode cn = new ClassNode();
    getClassReader(name).accept(cn, 0);
    return cn;
  }

  public boolean hasClass(String name) {
    return _entries.containsKey(name);
  }
}
