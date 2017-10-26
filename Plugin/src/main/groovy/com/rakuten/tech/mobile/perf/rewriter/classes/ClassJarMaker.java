package com.rakuten.tech.mobile.perf.rewriter.classes;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class ClassJarMaker {

  private final JarOutputStream _output;
  private final HashSet<String> _dirs = new HashSet<String>();

  public ClassJarMaker(File file) {
    try {
      file.getParentFile().mkdirs();
      Manifest manifest = new Manifest();
      manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
      _output = new JarOutputStream(new FileOutputStream(file), manifest);
    } catch (Exception e) {
      throw new RuntimeException("Failed to create jar", e);
    }
  }

  public void populate(String sources) {
    try {
      for (String source : sources.split(File.pathSeparator)) {
        File file = new File(source.trim());
        if (file.exists()) {
          populate(file, file);
        }
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to populate jar", e);
    }
  }

  private void populate(File source, File file) throws IOException {
    if (file.isDirectory()) {
      for (File child : file.listFiles()) {
        populate(source, child);
      }
    } else if (file.getName().toLowerCase().endsWith(".class")) {
      String name = Paths.get(source.getPath()).relativize(Paths.get(file.getPath())).toString();
      name = name.substring(0, name.length() - ".class".length()).replace(File.separatorChar, '.');
      add(name, new FileInputStream(file));
    } else if (file.getName().toLowerCase().endsWith(".jar")) {
      JarFile jar = null;
      try {
        jar = new JarFile(file);
        Enumeration<JarEntry> entries = jar.entries();
        while (entries.hasMoreElements()) {
          JarEntry entry = entries.nextElement();
          String name = entry.getName();
          if (name.toLowerCase().endsWith(".class")) {
            name = name.substring(0, name.length() - ".class".length()).replace('/', '.');
            add(name, jar.getInputStream(entry));
          }
        }
      } finally {
        if (jar != null) {
          jar.close();
        }
      }
    }
  }

  public void add(String name, InputStream input) {
    try {
      JarEntry entry = new JarEntry(name.replace('.', '/') + ".class");
      _output.putNextEntry(entry);

      BufferedInputStream bufferedInput = new BufferedInputStream(input);
      byte[] buffer = new byte[1024];
      int read;
      while ((read = bufferedInput.read(buffer)) > 0) {
        _output.write(buffer, 0, read);
      }
      _output.closeEntry();
    } catch (Exception e) {
      throw new RuntimeException("Failed to add jar entry", e);
    } finally {
      try {
        input.close();
      } catch (IOException e) {
      }
    }
  }

  public void add(String name, byte[] data) {
    try {
      JarEntry entry = new JarEntry(name.replace('.', '/') + ".class");
      _output.putNextEntry(entry);
      _output.write(data);
      _output.closeEntry();
    } catch (Exception e) {
      throw new RuntimeException("Failed to add jar entry", e);
    }
  }

  public void add(String name, ClassJar jar) {
    add(name, jar.getInputStream(name));
  }

  public void Close() {
    try {
      _output.close();
    } catch (IOException e) {
    }
  }

}
