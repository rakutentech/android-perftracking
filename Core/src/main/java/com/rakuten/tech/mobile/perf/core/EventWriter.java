package com.rakuten.tech.mobile.perf.core;

import android.util.Log;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import javax.net.ssl.HttpsURLConnection;

class EventWriter {

  private final String TAG = "Performance Tracking";
  private final Config _config;
  private final EnvironmentInfo _envInfo;
  private final URL _url;
  private HttpsURLConnection _conn;
  private BufferedWriter _writer;
  private int _measurements;

  EventWriter(Config config, EnvironmentInfo envInfo) {
    _config = config;
    _envInfo = envInfo;
    URL url = null;
    try {
      url = new URL(_config.eventHubUrl);
    } catch (MalformedURLException e) {
      if (_config.debug) {
        Log.d(TAG, e.getMessage());
      }
    } finally {
      _url = url;
    }
  }

  /* for testing */
  @SuppressWarnings("unused") EventWriter(Config config, EnvironmentInfo envInfo, URL url) {
    _config = config;
    _envInfo = envInfo;
    _url = url;
  }

  void begin() throws IOException {
    try {
      _conn = (HttpsURLConnection) _url.openConnection();
      _conn.setRequestMethod("POST");
      for (Map.Entry<String, String> entry : _config.header.entrySet()) {
        _conn.setRequestProperty(entry.getKey(), entry.getValue());
      }
      _conn.setUseCaches(false);
      _conn.setDoInput(false);
      _conn.setDoOutput(true);
      _conn.connect();

      _writer = new BufferedWriter(new OutputStreamWriter(_conn.getOutputStream()));
      _writer.append("{\"app\":\"").append(_config.app)
          .append("\",\"version\":\"").append(_config.version)
          .append("\",\"relay_app_id\":\"").append(_config.relayAppId);

      if (_envInfo.device != null) {
        _writer.append("\",\"device\":\"").append(_envInfo.device);
      }

      if (_envInfo.getAppUsedMemory() > 0) {
        _writer.append("\",\"app_mem_used\":\"").append(Long.toString(_envInfo.getAppUsedMemory()));
      }

      if (_envInfo.getDeviceFreeMemory() > 0) {
        _writer.append("\",\"device_mem_free\":\"").append(Long.toString(_envInfo.getDeviceFreeMemory()));
      }

      if (_envInfo.getDeviceTotalMemory() > 0) {
        _writer.append("\",\"device_mem_total\":\"").append(Long.toString(_envInfo.getDeviceTotalMemory()));
      }

      if (_envInfo.getBatteryLevel() > 0) {
        _writer.append("\",\"battery_level\":\"").append(Float.toString(_envInfo.getBatteryLevel()));
      }

      if (_envInfo.getCountry() != null) {
        _writer.append("\",\"country\":\"").append(_envInfo.getCountry());
      }

      if (_envInfo.getRegion() != null) {
        _writer.append("\",\"region\":\"").append(_envInfo.getRegion());
      }

      if (_envInfo.network != null) {
        _writer.append("\",\"network\":\"").append(_envInfo.network);
      }

      if (_envInfo.osName != null) {
        _writer.append("\",\"os\":\"").append(_envInfo.osName);
      }

      if (_envInfo.osVersion != null) {
        _writer.append("\",\"os_version\":\"").append(_envInfo.osVersion);
      }

      _writer.append("\",\"measurements\":[");
      _measurements = 0;

    } catch (Exception e) {
      if (_config.debug) {
        Log.d(TAG, e.getMessage());
      }
      disconnect();
      if (e instanceof IOException) {
        throw e;
      }
    }
  }

  void write(Metric metric) throws IOException {
    if (_writer != null) {
      try {
        if (_measurements > 0) {
          _writer.append(',');
        }
        _writer
            .append("{\"metric\":\"").append(metric.id)
            .append("\",\"urls\":").append(Long.toString(metric.urls))
            .append(",\"start\":").append(Long.toString(metric.startTime))
            .append(",\"time\":").append(Long.toString(metric.endTime - metric.startTime))
            .append('}');
        _measurements++;
      } catch (Exception e) {
        if (_config.debug) {
          Log.d(TAG, e.getMessage());
        }
        disconnect();
        if (e instanceof IOException) {
          throw e;
        }
      }
    }
  }

  void write(Measurement m, String metricId) throws IOException {
    if (_writer != null) {
      try {
        if (_measurements > 0) {
          _writer.append(',');
        }

        switch (m.type) {
          case Measurement.METHOD:
            _writer.append("{\"method\":\"").append((String) m.a).append('.').append((String) m.b)
                .append('"');
            break;

          case Measurement.URL:
            _writer.append("{\"url\":\"");

            if (m.a instanceof URL) {
              URL url = (URL) m.a;
              _writer.append(url.getProtocol()).append("://").append(url.getAuthority())
                  .append(escapeValue(url.getPath()));
            } else {
              String url = (String) m.a;
              int q = url.indexOf('?');
              if (q > 0) {
                url = url.substring(0, q);
              }
              _writer.append(escapeValue(url));
            }

            _writer.append('"');

            if (m.b != null) {
              _writer.append(",\"verb\":\"").append((String) m.b).append('"');
            }
            break;

          case Measurement.CUSTOM:
            _writer.append("{\"custom\":\"").append((String) m.a).append('"');
            break;

          default:
            return;
        }

        if (m.activityName != null && m.activityName.length() > 0) {
          _writer.append(",\"screen\":\"").append(m.activityName).append('"');
        }

        if (metricId != null) {
          _writer.append(",\"metric\":\"").append(metricId).append('"');
        }
        _writer.append(",\"start\":").append(Long.toString(m.startTime));
        _writer.append(",\"time\":").append(Long.toString(m.endTime - m.startTime)).append('}');
        _measurements++;
      } catch (Exception e) {
        if (_config.debug) {
          Log.d(TAG, e.getMessage());
        }
        disconnect();
        if (e instanceof IOException) {
          throw e;
        }
      }
    }
  }

  void end() throws IOException {
    try {
      if (_writer != null) {
        _writer.append("]}");
        _writer.close();

        int result = _conn.getResponseCode();

        if (result != 201) {
          throw new EventHubException(result);
        }
      }
    } catch (Exception e) {
      if (_config.debug) {
        Log.d(TAG, e.getMessage());
      }
      if (e instanceof EventHubException) {
        throw e;
      }
    } finally {
      disconnect();
    }
  }

  private void disconnect() {
    if (_conn != null) {
      _conn.disconnect();
      _conn = null;
    }
    if (_writer != null) {
      try {
        _writer.close();
      } catch (IOException e) {
        if (_config.debug) {
          Log.d(TAG, e.getMessage());
        }
      }
      _writer = null;
    }
  }

  private String escapeValue(String s) {
    return s.replace("\"", "\\\"");
  }
}
