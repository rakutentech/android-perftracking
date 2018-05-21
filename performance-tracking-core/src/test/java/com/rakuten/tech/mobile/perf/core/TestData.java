package com.rakuten.tech.mobile.perf.core;

import java.io.InputStream;
import java.util.Scanner;
import org.junit.rules.ExternalResource;

class TestData extends ExternalResource {

  private final String fileName;
  private Scanner scanner;
  String content = "";

  TestData(String resourceFileName) {
    this.fileName = resourceFileName;
  }

  @Override protected void before() {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream stream = classLoader.getResourceAsStream(fileName);
    if (stream == null) {
      return;
    }
    scanner = new Scanner(stream);
    StringBuilder sb = new StringBuilder();
    while (scanner.hasNext()) {
      sb.append(scanner.next());
    }
    content = sb.toString();
  }

  @Override protected void after() {
    scanner.close();
  }
}
