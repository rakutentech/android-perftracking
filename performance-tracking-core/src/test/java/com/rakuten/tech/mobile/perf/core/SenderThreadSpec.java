package com.rakuten.tech.mobile.perf.core;

import android.content.Context;

import java.util.concurrent.Callable;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.omg.CORBA.portable.OutputStream;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import javax.net.ssl.HttpsURLConnection;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.fail;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;

public class SenderThreadSpec {

  public static class MockedSender {

    private SenderThread senderThread;
    @Mock Sender sender;

    @Before public void init() {
      MockitoAnnotations.initMocks(this);
      senderThread = new SenderThread(sender, 10);
    }

    @After public void teardown() {
      senderThread.terminate();
      senderThread.interrupt();
    }

    @Test public void shouldStopThreadOnTerminate() throws IOException {
      when(sender.send(anyInt())).thenReturn(0);

      startSenderThread(senderThread);
      senderThread.terminate();

      await().until(senderThreadIsAlive(senderThread), equalTo(false));
    }

    @Test public void shouldCatchUncaughtExceptions() throws IOException {
      doThrow(IOException.class).when(sender).send(anyInt());

      final AtomicInteger count = new AtomicInteger(0);
      senderThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          count.incrementAndGet();
          fail("uncaught exception");
        }
      });
      startSenderThread(senderThread);

      verify(sender, timeout(100).atLeast(1)).send(anyInt());

      terminateSenderThread(senderThread);

      assertThat(count.get()).isEqualTo(0);
    }

    @Test public void shouldBackOffAfterFailures() throws IOException {
      doThrow(NullPointerException.class).when(sender).send(anyInt());

      startSenderThread(senderThread);

      verify(sender, timeout(100).times(4)).send(anyInt());
    }

    @Test public void shouldResetBackOffAfterSuccess() throws IOException {
      doThrow(NullPointerException.class).when(sender).send(anyInt());

      startSenderThread(senderThread);

      // Failures to increase the back off timeout
      verify(sender, timeout(100).atLeast(3)).send(anyInt());
      clearInvocations(sender);

      doReturn(0).when(sender).send(anyInt());

      // One success to reset the back off timeout
      verify(sender, timeout(100).atLeast(1)).send(anyInt());
      clearInvocations(sender);

      verify(sender, timeout(100).atLeast(1)).send(anyInt());
    }
  }

  public static class MockedWriter {

    @Mock EventWriter writer;
    SenderThread senderThread;
    private MeasurementBuffer buffer;

    @Before public void init() {
      MockitoAnnotations.initMocks(this);
      buffer = new MeasurementBuffer();
      Sender sender = new Sender(buffer, new Current(), writer, null, true);
      senderThread = new SenderThread(sender, 10);
    }

    @Test public void shouldBackOffAfterFailure() throws IOException {
      populateBufferWithDummyData(buffer, 20);
      doThrow(IOException.class).when(writer).begin();

      startSenderThread(senderThread);

      verify(writer, timeout(100).times(4)).begin();
    }

    @Test public void shouldResetBackOffAfterSuccess() throws IOException {
      populateBufferWithDummyData(buffer, 20);

      doThrow(IOException.class).when(writer).begin(); // fake connection failure
      senderThread.start();

      // Failures to increase the back off timeout
      verify(writer, timeout(100).atLeast(2)).begin();
      clearInvocations(writer);

      doNothing().when(writer).begin(); // no more failure

      verify(writer, timeout(100)).begin();
      verify(writer, timeout(100)).end();
      verify(writer, times(20)).write(any(Measurement.class), nullable(String.class));
    }
  }

  public static class MockedUrlConnection {

    private Config config;
    private EnvironmentInfo envInfo;
    @Mock URL url;
    @Mock OutputStream outputStream;
    @Mock HttpsURLConnection conn;
    @Mock Context ctx;
    private CachingObservable<LocationData> location = new CachingObservable<LocationData>(null);
    private CachingObservable<Float> batteryinfo = new CachingObservable<Float>(null);
    private EventWriter writer;
    private MeasurementBuffer buffer;
    SenderThread senderThread;

    @Before public void initMocks() throws IOException {
      MockitoAnnotations.initMocks(this);
      config = new Config();
      config.app = "app";
      config.version = "test-version";
      config.debug = true;
      config.eventHubUrl = ""; // url injected via constructor
      config.header = new HashMap<>();
      config.enablePerfTrackingEvents = true;

      envInfo = new EnvironmentInfo(ctx, location, batteryinfo);
      location.publish(new LocationData("test-land", "test-region"));
      envInfo.network = "test-network";
      envInfo.device = "test-device";

      when(url.openConnection()).thenReturn(conn);
      when(conn.getOutputStream()).thenReturn(outputStream);

      writer = new EventWriter(config, envInfo, url);

      buffer = new MeasurementBuffer();
      Sender sender = new Sender(buffer, new Current(), writer, null, true);
      senderThread = new SenderThread(sender, 10);
    }

    @Test public void shouldTerminateOn401Error() throws IOException {
      populateBufferWithDummyData(buffer, 20);
      when(conn.getResponseCode()).thenReturn(401);

      senderThread.start();

      await().until(senderThreadIsAlive(senderThread), equalTo(false));
    }

    @Test public void shouldBackOffOnOtherNon201() throws IOException {
      when(conn.getResponseCode()).thenReturn(400);

      // constantly fill buffer, so the sender will continue writing
      PopulateBufferThread populateThread = new PopulateBufferThread(buffer);
      populateThread.start();

      startSenderThread(senderThread);

      verify(conn, timeout(150).times(4)).getResponseCode();

      populateThread.terminate();
    }
  }

  static void startSenderThread(SenderThread senderThread) {
    senderThread.start();
    await().until(senderThreadIsAlive(senderThread));
  }

  static void terminateSenderThread(SenderThread senderThread) {
    senderThread.terminate();
    await().until(senderThreadIsAlive(senderThread), equalTo(false));
  }

  static Callable<Boolean> senderThreadIsAlive(final SenderThread senderThread) {
    return new Callable<Boolean>() {
      public Boolean call() {
        return senderThread.isAlive();
      }
    };
  }

  private static void populateBufferWithDummyData(MeasurementBuffer buffer, int count) {
    Measurement next = buffer.next();
    next.type = Measurement.METRIC;
    for (int i = 0; i < count; i++) {
      buffer.next().endTime = 10000000;
      buffer.next().startTime = 1;
    }
  }

  static class PopulateBufferThread extends Thread {
    private volatile boolean run = true;
    private MeasurementBuffer buffer;

    PopulateBufferThread(MeasurementBuffer buffer) {
      this.buffer = buffer;
    }

    @Override
    public void run() {
      while (run) {
        MeasurementBuffer b = buffer;
        if (b != null) {
          populateBufferWithDummyData(b, 10);
        } else {
          return;
        }
        try {
          Thread.sleep(5);
        } catch (InterruptedException ignored) {
        }
      }
    }

    public void terminate() {
      run = false;
    }
  }
}
