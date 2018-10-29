package com.rakuten.tech.mobile.perf.core.wrappers;

import com.rakuten.tech.mobile.perf.core.Tracker;
import java.io.IOException;
import java.io.InputStream;

public class HttpInputStreamWrapper extends InputStream {

  private final InputStream _stream;
  private final int _id;
  private final int _statusCode;
  private final String _cdnHeader;
  private final long _contentLength;

  public HttpInputStreamWrapper(InputStream stream, int id, int statusCode, String cdnHeader, long contentLength) {
    _stream = stream;
    _id = id;
    _statusCode = statusCode;
    _cdnHeader = cdnHeader;
    _contentLength = contentLength;
  }

  @Override
  public int available() throws IOException {
    return _stream.available();
  }

  @Override
  public void close() throws IOException {
    Tracker.endUrl(_id, _statusCode, _cdnHeader, _contentLength);
    _stream.close();
  }

  @Override
  public synchronized void mark(int readlimit) {
    _stream.mark(readlimit);
  }

  @Override
  public boolean markSupported() {
    return _stream.markSupported();
  }

  @Override
  public int read() throws IOException {
    return _stream.read();
  }

  @Override
  public int read(byte[] b) throws IOException {
    return _stream.read(b);
  }

  @Override
  public int read(byte[] b, int off, int len) throws IOException {
    return _stream.read(b, off, len);
  }

  @Override
  public synchronized void reset() throws IOException {
    _stream.reset();
  }

  @Override
  public long skip(long n) throws IOException {
    return _stream.skip(n);
  }
}
