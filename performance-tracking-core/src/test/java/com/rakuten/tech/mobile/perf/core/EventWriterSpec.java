package com.rakuten.tech.mobile.perf.core;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.Context;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import javax.net.ssl.HttpsURLConnection;
import junit.framework.TestCase;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.omg.CORBA.portable.OutputStream;
import org.skyscreamer.jsonassert.JSONAssert;

@Ignore
public class EventWriterSpec {

  Config config;
  EnvironmentInfo envInfo;
  @Mock URL url;
  @Mock OutputStream outputStream;
  @Mock HttpsURLConnection conn;
  @Mock Context ctx;
  private final CachingObservable<LocationData> location = new CachingObservable<>(null);
  private final CachingObservable<Float> batteryinfo = new CachingObservable<Float>(null);
  EventWriter writer;

  @Before public void initMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    config = new Config();
    config.app = "app";
    config.version = "test-version";
    config.relayAppId = "test-relay-app-id";
    config.debug = true;
    config.eventHubUrl = ""; // url injected via constructor
    config.header = new HashMap<>();
    config.enablePerfTrackingEvents = true;
    envInfo = new EnvironmentInfo(ctx, location, batteryinfo);
    location.publish(new LocationData("test-land", "test-region"));
    envInfo.network = "test-network";
    envInfo.device = "test-device";
    envInfo.osVersion = "7.1.1";

    when(url.openConnection()).thenReturn(conn);
    when(conn.getOutputStream()).thenReturn(outputStream);
    when(conn.getResponseCode()).thenReturn(201);
  }

  public static class NormalEventWriterBehaviorSpec extends EventWriterSpec {
    @Before public void setup() throws IOException {
      writer = new EventWriter(config, envInfo, url);
      assertThat(writer).isNotNull();
    }

    @Rule public TestData emptyMyJson = new TestData("memory_measurement.json");

    @Test public void shouldWriteMemoryAndBatteryDetailsIntoMeasurement()
        throws IOException, JSONException {

      EnvironmentInfo myEnvInfo = Mockito.spy(envInfo);
      doReturn(2000L).when(myEnvInfo).getDeviceTotalMemory();
      doReturn(500L).when(myEnvInfo).getDeviceFreeMemory();
      doReturn(0.3f).when(myEnvInfo).getBatteryLevel();
      doReturn(100L).when(myEnvInfo).getAppUsedMemory();

      EventWriter myWriter = new EventWriter(config, myEnvInfo, url);
      myWriter.begin();
      myWriter.end();

      String writtenJson = extractWrittenString(outputStream);
      JSONAssert.assertEquals(emptyMyJson.content, writtenJson, true);

    }
    // creation & init

    @Test public void shouldOpenConnectionOnBegin() throws IOException {
      writer = new EventWriter(config, envInfo, null);

      writer.begin();

      // Verify no exception
    }

    @Test public void shouldDisconnectOnFailure() throws IOException {
      when(conn.getOutputStream()).thenThrow(new IOException());

      try {
        writer.begin();
      } catch (IOException ignored) {
      }

      verify(conn).disconnect();
    }

    @Test public void shouldFailSilentlyOnBadUrl() throws IOException {

      writer.begin();
      verify(url).openConnection();
      verify(conn).getOutputStream();
    }

    @Test public void shouldConfigureConnectionOnBegin() throws IOException {
      config.header.put("test-header", "test-header-value");
      writer.begin();
      verify(conn).setRequestMethod("POST");
      verify(conn).setRequestProperty("test-header", "test-header-value");
      verify(conn).setUseCaches(false);
      verify(conn).setDoInput(false);
      verify(conn).setDoOutput(true);
    }

    // writing

    @Rule public TestData emptyJson = new TestData("no_measurement.json");

    @Test public void shouldWriteConfigAndEnvInfo() throws IOException, JSONException {
      writer.begin();
      writer.end();

      String writtenJson = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(emptyJson.content, writtenJson, true);
    }

    @Rule public TestData emptyNoEnvJson = new TestData("no_measurement_no_env.json");

    @Test public void shouldHandleNullsInEnvInfo() throws IOException, JSONException {
      envInfo.device = null;
      envInfo.network = null;

      writer.begin();
      writer.end();

      String writtenJson = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(emptyNoEnvJson.content, writtenJson, true);
    }

    @Rule public TestData metricJson = new TestData("metric.json");

    @Test public void shouldWriteSingleMetric() throws IOException, JSONException {
      Metric metric = new Metric();
      metric.startTime = 0L;
      metric.endTime = 999;
      metric.id = "test-metric";
      metric.urls = 999;

      writer.begin();
      writer.write(metric);
      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(metricJson.content, writtenString, true);
    }

    @Rule public TestData methodMeasurementJson = new TestData("method_measurement.json");

    @Test public void shouldWriteSingleMethodMeasurement() throws JSONException, IOException {
      Measurement measurement = new Measurement();
      measurement.type = Measurement.METHOD;
      measurement.a = "TestClass";
      measurement.b = "testMethod";
      measurement.startTime = 0L;
      measurement.endTime = 999;

      writer.begin();
      writer.write(measurement, "test-metric");
      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(methodMeasurementJson.content, writtenString, true);
    }

    @Rule public TestData urlMeasurementJson = new TestData("url_measurement.json");

    @Test public void shouldWriteUrlObjectMeasurement() throws JSONException, IOException {
      Measurement measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = new URL("https://rakuten.co.jp/some/path?and=some&url=params");
      measurement.b = "VERB";
      measurement.c = 200;
      measurement.startTime = 0L;
      measurement.endTime = 999;

      writer.begin();
      writer.write(measurement, "test-metric");
      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(urlMeasurementJson.content, writtenString, true);
    }

    @Test public void shouldWriteUrlStringMeasurement() throws JSONException, IOException {
      Measurement measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = "https://rakuten.co.jp/some/path?and=some&url=params";
      measurement.b = "VERB";
      measurement.c = 200;
      measurement.startTime = 0L;
      measurement.endTime = 999;

      writer.begin();
      writer.write(measurement, "test-metric");
      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(urlMeasurementJson.content, writtenString, true);
    }

    @Rule public TestData customMeasurementJson = new TestData("custom_measurement.json");

    @Test public void shouldWriteSingleCustomMeasurement() throws JSONException, IOException {
      Measurement measurement = new Measurement();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = 0L;
      measurement.endTime = 999;
      measurement.activityName = "test-activity";

      writer.begin();
      writer.write(measurement, "test-metric");
      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(customMeasurementJson.content, writtenString, true);
    }

    @Rule public TestData escapedJson = new TestData("escaped.json");

    @Test public void shouldHandleMalformedURLData() throws IOException, JSONException {
      Measurement measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = new URL("http://example.com:80/page1\".html");
      measurement.startTime = 0L;
      measurement.endTime = 999;

      writer.begin();
      writer.write(measurement, "test-metric");
      measurement.a = new URL("http://example.com:80/page1\"[.html");
      writer.write(measurement, "test-metric");
      measurement.a = new URL("http://example.com:80/page1\"{.html");
      writer.write(measurement, "test-metric");
      measurement.a = new URL("http://example.com:80/page1\",.html");
      writer.write(measurement, "test-metric");
      measurement.a = new URL("http://example.com:80/page1'}'.html");
      writer.write(measurement, "test-metric");
      //For URL as String.
      measurement.a = "http://example.com:80/page1\".html";
      writer.write(measurement, "test-metric");
      measurement.a = "http://example.com:80/page1\"[.html";
      writer.write(measurement, "test-metric");
      measurement.a = "http://example.com:80/page1\"{.html";
      writer.write(measurement, "test-metric");
      measurement.a = "http://example.com:80/page1\",.html";
      writer.write(measurement, "test-metric");
      measurement.a = "http://example.com:80/page1'}'.html";
      writer.write(measurement, "test-metric");
      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(escapedJson.content, writtenString, true);
    }

    // smoke test

    @Rule public TestData smokeTestJson = new TestData("smoke_test.json");

    @Test public void smokeTest() throws IOException, JSONException {
      Metric metric = new Metric();
      metric.startTime = 0L;
      metric.endTime = 999;
      metric.id = "test-metric";
      metric.urls = 999;

      writer.begin();
      writer.write(metric);
      writer.write(metric);

      Measurement measurement = new Measurement();
      measurement.type = Measurement.METHOD;
      measurement.a = "TestClass";
      measurement.b = "testMethod";
      measurement.startTime = 1L;
      measurement.endTime = 999;

      writer.write(measurement, metric.id);

      measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = "https://rakuten.co.jp1/some/path?and=some&url=params";
      measurement.b = "VERB";
      measurement.startTime = 10L;
      measurement.endTime = 999;

      writer.write(measurement, metric.id);

      measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = "https://rakuten.co.jp2/some/path?and=some&url=params";
      measurement.b = "VERB";
      measurement.startTime = 11L;
      measurement.endTime = 999;

      writer.write(measurement, null);

      measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = "https://rakuten.co.jp3/some/path?and=some&url=params";
      measurement.b = null;
      measurement.startTime = 100L;
      measurement.endTime = 999;

      writer.write(measurement, null);

      measurement = new Measurement();
      measurement.type = Measurement.CUSTOM;
      measurement.a = "custom-measurement";
      measurement.startTime = 101L;
      measurement.endTime = 999;

      writer.write(measurement, metric.id);

      measurement = new Measurement();
      measurement.type = Measurement.URL;
      measurement.a = new URL("https://amazon.co.jp/other/path?and=some&url=params");
      measurement.b = "BERV";
      measurement.startTime = 0L;
      measurement.endTime = 100;

      writer.write(measurement, metric.id);

      writer.end();

      String writtenString = trimAppMemDetails(extractWrittenString(outputStream));
      JSONAssert.assertEquals(smokeTestJson.content, writtenString, true);
    }

    // helpers

    private String extractWrittenString(OutputStream outputStream) throws IOException {
      ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
      verify(outputStream).write(captor.capture(), anyInt(), anyInt());
      byte[] writtenBytes = captor.getValue();
      assertThat(writtenBytes).isNotEmpty();
      return new String(writtenBytes);
    }

    /**
     * Removes a key with name `app_mem_used` and its value from the input string
     * Due to limitation we couldn't mock Runtime class native methods.
     * Because of this we cannot validate App used memory in unit test. So removing `app_mem_used` field from actual string.
     *
     * @param input String
     * @return String
     */
    private static String trimAppMemDetails(String input) {
      JSONObject data = null;
      try {
        data = new JSONObject(input);
        data.remove("app_mem_used");
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return data != null ? data.toString() : "{}";
    }
  }

  public static class EventWriterExceptionHandlingSpec extends EventWriterSpec {

    @Before
    public void setup() {
      writer = new EventWriter(config, envInfo, url);
    }

    @Test public void shouldNotFailOnBadUrl() {
      try {
        config.eventHubUrl = "usnteaueau";
        writer = new EventWriter(config, envInfo);
      } catch(Exception e) {
        TestCase.fail();
      }
    }

    @Test public void shouldNotFailOnGoodUrl() {
      try {
        config.eventHubUrl = "https://rakuten.co.jp";
        writer = new EventWriter(config, envInfo);
      } catch(Exception e) {
        TestCase.fail();
      }
    }

    @Test public void shouldNotFailOnWritingNull() {
      try {
        writer.write(null);
        writer.write(null, null);

        writer.begin();
        writer.write(null);
        writer.write(null, null);
        writer.end();
      } catch(Exception e) {
        TestCase.fail();
      }
    }

    @Test public void shouldNotFailOnWriteWithoutBegin() {
      try {
        Measurement measurement = new Measurement();
        measurement.type = Measurement.CUSTOM;
        measurement.a = "";
        measurement.startTime = 0L;
        measurement.endTime = 999;
        writer.write(measurement, "");
      } catch(Exception e) {
        TestCase.fail();
      }
    }

    @Test public void shouldNotFailOnNullPayload() {
      try {
        Measurement measurement = new Measurement();
        measurement.type = Measurement.CUSTOM;
        measurement.a = null;
        measurement.b = null;
        measurement.startTime = 0L;
        measurement.endTime = 999;
        writer.begin();
        writer.write(measurement, null);
        measurement.type = Measurement.METHOD;
        writer.write(measurement, null);
        measurement.type = Measurement.URL;
        writer.write(measurement, null);
        measurement.type = Measurement.METRIC;
        writer.write(measurement, null);
        measurement.type = 5; // invalid type
        writer.write(measurement, null);

        Metric metric = new Metric();
        metric.startTime = 0L;
        metric.endTime = 999;
        metric.id = null;
        metric.urls = 999;
        writer.write(metric);
        writer.end();
      } catch(Exception e) {
        TestCase.fail();
      }
    }

    @Test public void shouldNotFailOnIncorrectEnd() {
      try {
        writer.end();
        writer.begin();
        writer.end();
        writer.end();
      } catch(Exception e) {
        TestCase.fail();
      }
    }
  }

  public static class DisabledEventWriterBehaviorSpec extends EventWriterSpec {

    @Before
    public void setup() {
      config.enablePerfTrackingEvents = false;

      writer = new EventWriter(config, envInfo, url);
    }

    @Test
    public void shouldNotMakeConnections() throws IOException {
      writer.begin();
      writer.end();

      verify(url, never()).openConnection();
      verify(conn, never()).disconnect();
    }

    @Test
    public void shouldNotWriteEvents() throws IOException {
      writer.begin();
      writer.write(new Metric());
      writer.write(new Measurement(), "metric_id");
      writer.end();

      verify(outputStream, never()).write(any(byte[].class), anyInt(), anyInt());
    }
  }
}
