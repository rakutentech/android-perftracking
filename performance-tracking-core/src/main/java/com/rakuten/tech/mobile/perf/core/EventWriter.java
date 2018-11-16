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
  private final Config config;
  private final EnvironmentInfo envInfo;
  private final URL url;
  private HttpsURLConnection connection;
  private BufferedWriter writer;
  private int measurements;

  EventWriter(Config config, EnvironmentInfo envInfo) {
    this.config = config;
    this.envInfo = envInfo;
    URL url = null;
    try {
      url = new URL(this.config.eventHubUrl);
    } catch (MalformedURLException e) {
      if (this.config.debug) {
        Log.d(TAG, e.getMessage());
      }
    } finally {
      this.url = url;
    }
  }

  /* for testing */
  @SuppressWarnings("unused") EventWriter(Config config, EnvironmentInfo envInfo, URL url) {
    this.config = config;
    this.envInfo = envInfo;
    this.url = url;
  }

  void begin() throws IOException {
    if (!config.enablePerfTrackingEvents) {
      return;
    }

    try {
      connection = (HttpsURLConnection) url.openConnection();
      connection.setRequestMethod("POST");
      for (Map.Entry<String, String> entry : config.header.entrySet()) {
        connection.setRequestProperty(entry.getKey(), entry.getValue());
      }
      connection.setUseCaches(false);
      connection.setDoInput(false);
      connection.setDoOutput(true);
      connection.connect();

      writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream()));
      writer.append("{\"app\":\"").append(config.app).append("\"")
          .append(",\"version\":\"").append(config.version).append("\"")
          .append(",\"relay_app_id\":\"").append(config.relayAppId).append("\"");

      if (envInfo.device != null) {
        writer.append(",\"device\":\"").append(envInfo.device).append("\"");
      }

      if (envInfo.getAppUsedMemory() > 0) {
        writer.append(",\"app_mem_used\":").append(Long.toString(envInfo.getAppUsedMemory()));
      }

      if (envInfo.getDeviceFreeMemory() > 0) {
        writer.append(",\"device_mem_free\":").append(Long.toString(envInfo.getDeviceFreeMemory()));
      }

      if (envInfo.getDeviceTotalMemory() > 0) {
        writer.append(",\"device_mem_total\":").append(Long.toString(envInfo.getDeviceTotalMemory()));
      }

      if (envInfo.getBatteryLevel() > 0) {
        writer.append(",\"battery_level\":").append(Float.toString(envInfo.getBatteryLevel()));
      }

      if (envInfo.getCountry() != null) {
        writer.append(",\"country\":\"").append(envInfo.getCountry()).append("\"");
      }

      if (envInfo.getRegion() != null) {
        writer.append(",\"region\":\"").append(envInfo.getRegion()).append("\"");
      }

      if (envInfo.network != null) {
        writer.append(",\"network\":\"").append(envInfo.network).append("\"");
      }

      if (envInfo.osName != null) {
        writer.append(",\"os\":\"").append(envInfo.osName).append("\"");
      }

      if (envInfo.osVersion != null) {
        writer.append(",\"os_version\":\"").append(envInfo.osVersion).append("\"");
      }

      writer.append(",\"measurements\":[");
      measurements = 0;

    } catch (Exception e) {
      if (config.debug) {
        Log.d(TAG, e.getMessage());
      }
      disconnect();
      if (e instanceof IOException) {
        throw e;
      }
    }
  }

  void write(Metric metric) throws IOException {
    if (!config.enablePerfTrackingEvents || writer == null) {
      return;
    }

    try {
      if (measurements > 0) {
        writer.append(',');
      }
      writer
          .append("{\"metric\":\"").append(metric.id).append("\"")
          .append(",\"urls\":").append(Long.toString(metric.urls))
          .append(",\"start\":").append(Long.toString(metric.startTime))
          .append(",\"time\":").append(Long.toString(metric.endTime - metric.startTime))
          .append('}');
      measurements++;
    } catch (Exception e) {
      if (config.debug) {
        Log.d(TAG, e.getMessage());
      }
      disconnect();
      if (e instanceof IOException) {
        throw e;
      }
    }
  }

  void write(Measurement m, String metricId) throws IOException {
    if (!config.enablePerfTrackingEvents || writer == null) {
      return;
    }

    try {
      if (measurements > 0) {
        writer.append(',');
      }

      switch (m.type) {
        case Measurement.METHOD:
          writer.append("{\"method\":\"").append((String) m.a).append('.').append((String) m.b)
              .append('"');
          break;

        case Measurement.URL:
          writer.append("{\"url\":\"");

          if (m.a instanceof URL) {
            URL url = (URL) m.a;
            writer.append(url.getProtocol()).append("://").append(url.getAuthority())
                .append(escapeValue(url.getPath()));
          } else {
            String url = (String) m.a;
            int q = url.indexOf('?');
            if (q > 0) {
              url = url.substring(0, q);
            }
            writer.append(escapeValue(url));
          }

          writer.append('"');

          if (m.b != null) {
            writer.append(",\"verb\":\"").append((String) m.b).append('"');
          }
          if (m.c != null) {
            writer.append(",\"status_code\":").append(m.c.toString());
          }
          break;

        case Measurement.CUSTOM:
          writer.append("{\"custom\":\"").append((String) m.a).append('"');
          break;

        default:
          return;
      }

      if (m.activityName != null && m.activityName.length() > 0) {
        writer.append(",\"screen\":\"").append(m.activityName).append('"');
      }

      if (metricId != null) {
        writer.append(",\"metric\":\"").append(metricId).append('"');
      }
      writer.append(",\"start\":").append(Long.toString(m.startTime));
      writer.append(",\"time\":").append(Long.toString(m.endTime - m.startTime)).append('}');
      measurements++;
    } catch (Exception e) {
      if (config.debug) {
        Log.d(TAG, e.getMessage());
      }
      disconnect();
      if (e instanceof IOException) {
        throw e;
      }
    }
  }

  void end() throws IOException {
    if (!config.enablePerfTrackingEvents) {
      return;
    }

    try {
      if (writer != null) {
        writer.append("]}");
        writer.close();

        int result = connection.getResponseCode();

        if (result != 201) {
          throw new EventHubException(result);
        }
      }
    } catch (Exception e) {
      if (config.debug) {
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
    if (connection != null) {
      connection.disconnect();
      connection = null;
    }
    if (writer != null) {
      try {
        writer.close();
      } catch (IOException e) {
        if (config.debug) {
          Log.d(TAG, e.getMessage());
        }
      }
      writer = null;
    }
  }

  private String escapeValue(String s) {
    return s.replace("\"", "\\\"");
  }
}
