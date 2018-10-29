package com.rakuten.tech.mobile.perf.core.wrappers;

import com.rakuten.tech.mobile.perf.core.Analytics;
import com.rakuten.tech.mobile.perf.core.Tracker;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.security.Permission;
import java.util.List;
import java.util.Map;

public class HttpURLConnectionWrapper extends HttpURLConnection {

  private final int NOT_STARTED = 0;
  private final int STARTED = 1;
  private final int ENDED = 2;
  private final int INPUT = 3;

  private final HttpURLConnection _conn;
  private int _state;
  private int _id;

  public HttpURLConnectionWrapper(HttpURLConnection conn) {
    super(conn.getURL());
    _conn = conn;
  }

  // HttpURLConnection

  @Override
  public void disconnect() {
    _conn.disconnect();

    if (_state == STARTED) {
      Tracker.endUrl(_id, 0, null, 0);
      _state = ENDED;
    }
  }

  @Override
  public InputStream getErrorStream() {
    return _conn.getErrorStream();
  }

  @Override
  public boolean getInstanceFollowRedirects() {
    return _conn.getInstanceFollowRedirects();
  }

  @Override
  public String getRequestMethod() {
    return _conn.getRequestMethod();
  }

  @Override
  public int getResponseCode() throws IOException {
    if (_state == NOT_STARTED) {
      _id = Tracker.startUrl(url, _conn.getRequestMethod());
      _state = STARTED;
    }

    int result = _conn.getResponseCode();


    if ((_state == STARTED) && (!getDoInput())) {
      Tracker.endUrl(_id, result, _conn.getHeaderField(Analytics.CDN_HEADER), _conn.getContentLengthLong());
      _state = ENDED;
    }

    return result;
  }

  @Override
  public String getResponseMessage() throws IOException {
    if (_state == NOT_STARTED) {
      _id = Tracker.startUrl(url, _conn.getRequestMethod());
      _state = STARTED;
    }

    String result = _conn.getResponseMessage();

    if ((_state == STARTED) && (!getDoInput())) {
      Tracker.endUrl(_id, _conn.getResponseCode(), _conn.getHeaderField(Analytics.CDN_HEADER), _conn.getContentLengthLong());
      _state = ENDED;
    }

    return result;
  }

  @Override
  public void setChunkedStreamingMode(int chunklen) {
    _conn.setChunkedStreamingMode(chunklen);
  }

  @Override
  public void setFixedLengthStreamingMode(int contentLength) {
    _conn.setFixedLengthStreamingMode(contentLength);
  }

  @Override
  public void setInstanceFollowRedirects(boolean followRedirects) {
    _conn.setInstanceFollowRedirects(followRedirects);
  }

  @Override
  public void setRequestMethod(String method) throws ProtocolException {
    _conn.setRequestMethod(method);
  }

  @Override
  public boolean usingProxy() {
    return _conn.usingProxy();
  }

  // URLConnection

  @Override
  public void addRequestProperty(String key, String value) {
    _conn.addRequestProperty(key, value);
  }

  @Override
  public void connect() throws IOException {
    if (_state == NOT_STARTED) {
      _id = Tracker.startUrl(url, _conn.getRequestMethod());
      _state = STARTED;
    }

    _conn.connect();
  }

  @Override
  public boolean getAllowUserInteraction() {
    return _conn.getAllowUserInteraction();
  }

  @Override
  public int getConnectTimeout() {
    return _conn.getConnectTimeout();
  }

  @Override
  public Object getContent() throws IOException {
    return _conn.getContent();
  }

  @Override
  public Object getContent(Class[] classes) throws IOException {
    return _conn.getContent(classes);
  }

  @Override
  public String getContentEncoding() {
    return _conn.getContentEncoding();
  }

  @Override
  public int getContentLength() {
    return _conn.getContentLength();
  }

  @Override
  public String getContentType() {
    return _conn.getContentType();
  }

  @Override
  public long getDate() {
    return _conn.getDate();
  }

  @Override
  public boolean getDefaultUseCaches() {
    return _conn.getDefaultUseCaches();
  }

  @Override
  public boolean getDoInput() {
    return _conn.getDoInput();
  }

  @Override
  public boolean getDoOutput() {
    return _conn.getDoOutput();
  }

  @Override
  public long getExpiration() {
    return _conn.getExpiration();
  }

  @Override
  public String getHeaderField(String name) {
    return _conn.getHeaderField(name);
  }

  @Override
  public String getHeaderField(int n) {
    return _conn.getHeaderField(n);
  }

  @Override
  public long getHeaderFieldDate(String name, long Default) {
    return _conn.getHeaderFieldDate(name, Default);
  }

  @Override
  public int getHeaderFieldInt(String name, int Default) {
    return _conn.getHeaderFieldInt(name, Default);
  }

  @Override
  public String getHeaderFieldKey(int n) {
    return _conn.getHeaderFieldKey(n);
  }

  @Override
  public Map<String, List<String>> getHeaderFields() {
    return _conn.getHeaderFields();
  }

  @Override
  public long getIfModifiedSince() {
    return _conn.getIfModifiedSince();
  }

  @Override
  public InputStream getInputStream() throws IOException {
    if (_state == NOT_STARTED) {
      _id = Tracker.startUrl(url, _conn.getRequestMethod());
      _state = STARTED;
    }

    int statusCode = _conn.getResponseCode();
    String cdnHeader = _conn.getHeaderField(Analytics.CDN_HEADER);
    long contentLength = _conn.getContentLengthLong();
    try {
      InputStream stream = _conn.getInputStream();

      if (stream == null) {
        if (_state == STARTED) {
          Tracker.endUrl(_id, statusCode, cdnHeader, contentLength);
          _state = ENDED;
        }
        return null;
      }

      if (_state == STARTED) {
        _state = INPUT;
      }

      return new HttpInputStreamWrapper(stream, _id, statusCode, cdnHeader, contentLength);
    } catch (IOException e) {
      if (_state == STARTED) {
        Tracker.endUrl(_id, statusCode, cdnHeader, contentLength);
        _state = ENDED;
      }
      throw e;
    }
  }

  @Override
  public long getLastModified() {
    return _conn.getLastModified();
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return _conn.getOutputStream();
  }

  @Override
  public Permission getPermission() throws IOException {
    return _conn.getPermission();
  }

  @Override
  public int getReadTimeout() {
    return _conn.getReadTimeout();
  }

  @Override
  public Map<String, List<String>> getRequestProperties() {
    return _conn.getRequestProperties();
  }

  @Override
  public String getRequestProperty(String key) {
    return _conn.getRequestProperty(key);
  }

  @Override
  public URL getURL() {
    return _conn.getURL();
  }

  @Override
  public boolean getUseCaches() {
    return _conn.getUseCaches();
  }

  @Override
  public void setAllowUserInteraction(boolean allowuserinteraction) {
    _conn.setAllowUserInteraction(allowuserinteraction);
  }

  @Override
  public void setConnectTimeout(int timeout) {
    _conn.setConnectTimeout(timeout);
  }

  @Override
  public void setDefaultUseCaches(boolean defaultusecaches) {
    _conn.setDefaultUseCaches(defaultusecaches);
  }

  @Override
  public void setDoInput(boolean doinput) {
    _conn.setDoInput(doinput);
  }

  @Override
  public void setDoOutput(boolean dooutput) {
    _conn.setDoOutput(dooutput);
  }

  @Override
  public void setIfModifiedSince(long ifmodifiedsince) {
    _conn.setIfModifiedSince(ifmodifiedsince);
  }

  @Override
  public void setReadTimeout(int timeout) {
    _conn.setReadTimeout(timeout);
  }

  @Override
  public void setRequestProperty(String key, String value) {
    _conn.setRequestProperty(key, value);
  }

  @Override
  public void setUseCaches(boolean usecaches) {
    _conn.setUseCaches(usecaches);
  }

  @Override
  public String toString() {
    return _conn.toString();
  }
}
