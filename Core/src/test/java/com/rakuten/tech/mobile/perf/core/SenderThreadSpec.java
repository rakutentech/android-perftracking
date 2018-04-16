package com.rakuten.tech.mobile.perf.core;

import android.content.Context;

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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
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

    @Test public void shouldStopThreadOnTerminate() throws InterruptedException, IOException {
      when(sender.send(anyInt())).thenReturn(0);

      senderThread.start();
      Thread.sleep(100);
      senderThread.terminate();

      loop(senderThread, 1);

      assertThat(senderThread.isAlive()).isFalse();
    }

    @Test public void shouldCatchUncaughtExceptions() throws InterruptedException, IOException {
      doThrow(IOException.class).when(sender).send(anyInt());

      final AtomicInteger count = new AtomicInteger(0);
      senderThread.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
        @Override
        public void uncaughtException(Thread t, Throwable e) {
          count.incrementAndGet();
          fail("uncaught exception");
        }
      });
      senderThread.start();
      Thread.sleep(100);
      senderThread.terminate();
      loop(senderThread, 1);

      assertThat(count.get()).isEqualTo(0);
    }

    @Test public void shouldBackOffAfterFailures() throws InterruptedException, IOException {
      doThrow(NullPointerException.class).when(sender).send(anyInt());

      senderThread.start();
      Thread.sleep(140);
      senderThread.terminate();
      loop(senderThread, 1); // give it some time to finish run()

      verify(sender, atMost(4)).send(anyInt());
    }

    @Test public void shouldResetBackOffAfterSuccess() throws InterruptedException,
        IOException {
      doThrow(NullPointerException.class).when(sender).send(anyInt());

      senderThread.start();
      Thread.sleep(140);

      clearInvocations(sender);
      doReturn(0).when(sender).send(anyInt());
      loop(senderThread, 1); // give it some time to finish run()

      Thread.sleep(50);

      verify(sender, atLeast(4)).send(anyInt());
    }


    @Test public void shouldResetFailureCountAfterSuccess()
        throws InterruptedException, IOException {
      doThrow(NullPointerException.class).when(sender).send(anyInt());

      senderThread.start();
      // 15 failures
      loop(senderThread, 15);

      doReturn(0).when(sender).send(anyInt());
      // one success
      loop(senderThread, 1);

      // 15 failures
      loop(senderThread, 15);

      assertThat(senderThread.isAlive()).isTrue();
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

    @Test public void shouldBackOffAfterFailure() throws InterruptedException,
        IOException {
      populateBufferWithDummyData(buffer, 20);

      doThrow(IOException.class).when(writer).begin();

      senderThread.start();
      Thread.sleep(140);

      verify(writer, atMost(4)).begin();
    }

    @Test public void shouldResetBackOffAfterSuccess() throws InterruptedException,
        IOException {
      populateBufferWithDummyData(buffer, 20);

      doThrow(IOException.class).when(writer).begin(); // fake connection failure
      senderThread.start();
      Thread.sleep(50); // 2 failures -> 2^2*sleeptime interval

      clearInvocations(writer);
      doNothing().when(writer).begin(); // no more failure

      loop(senderThread, 1); // interrupt sleep

      verify(writer).begin();
      verify(writer, times(20)).write(any(Measurement.class), nullable(String.class));
      verify(writer).end();
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
    private Runnable populateBufferRunnable;
    SenderThread senderThread;

    @Before public void initMocks() throws IOException {
      MockitoAnnotations.initMocks(this);
      config = new Config();
      config.app = "app";
      config.version = "test-version";
      config.debug = true;
      config.eventHubUrl = ""; // url injected via constructor
      config.header = new HashMap<>();

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
      populateBufferRunnable = new Runnable() {
        @Override
        public void run() {
          while (true) {
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
      };
    }

    @Test public void shouldTerminateOn401Error() throws IOException, InterruptedException {
      populateBufferWithDummyData(buffer, 20);
      when(conn.getResponseCode()).thenReturn(401);

      senderThread.start();

      Thread.sleep(100); // give the thread some time to die

      assertThat(senderThread.isAlive()).isFalse();
    }

    @Test public void shouldBackOffOnOtherNon201() throws IOException, InterruptedException {
      when(conn.getResponseCode()).thenReturn(400);

      // constantly fill buffer, so the sender will continue writing
      new Thread(populateBufferRunnable).start();
      senderThread.start();

      Thread.sleep(140);

      verify(conn, atMost(5)).getResponseCode();
    }

  }


  private static void loop(SenderThread senderThread, int i) throws InterruptedException {
    if (i < 1) {
      return;
    }
    for (; i > 0; i--) {
      senderThread.interrupt();
      Thread.sleep(5);
    }
  }

  private static void populateBufferWithDummyData(MeasurementBuffer buffer, int count) {
    Measurement next = buffer.next();
    next.type = Measurement.METRIC;
    for (int i = 0; i < count; i++) {
      buffer.next().endTime = 10000000;
      buffer.next().startTime = 1;
    }
  }


}
