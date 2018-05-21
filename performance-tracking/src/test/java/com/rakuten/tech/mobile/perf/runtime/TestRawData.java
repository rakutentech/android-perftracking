package com.rakuten.tech.mobile.perf.runtime;

import java.io.IOException;
import java.io.InputStream;
import okio.Okio;
import org.junit.rules.ExternalResource;

public class TestRawData extends ExternalResource {

  private final String fileName;
  public byte[] data;

  public TestRawData(String resourceFileName) {
    this.fileName = resourceFileName;
  }

  @Override protected void before() throws IOException {
    ClassLoader classLoader = this.getClass().getClassLoader();
    InputStream stream = classLoader.getResourceAsStream(fileName);
    if (stream == null) {
      return;
    }
    data = Okio.buffer(Okio.source(stream)).readByteArray();
  }
}
