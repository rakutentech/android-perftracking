package com.rakuten.tech.mobile.perf.runtime;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import okio.Okio;
import org.junit.rules.ExternalResource;

public class TestData extends ExternalResource {

  private final String fileName;
  public String content = "";

  public TestData(String resourceFileName) {
    this.fileName = resourceFileName;
  }

  @Override
  protected void before() throws IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream stream = classLoader.getResourceAsStream(fileName);
    if (stream == null) {
      return;
    }
    content = Okio.buffer(Okio.source(stream)).readString(Charset.forName("UTF-8"));
  }
}
