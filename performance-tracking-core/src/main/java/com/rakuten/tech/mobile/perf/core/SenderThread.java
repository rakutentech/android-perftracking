package com.rakuten.tech.mobile.perf.core;

class SenderThread extends Thread {

  private static final int SLEEP_INTERVAL_MILLISECONDS = 10_000;
  private static final int SLEEP_MAX_INTERVAL_MILLISECONDS = 1_800_000;

  private final Sender _sender;
  private final int _interval;
  private volatile boolean _run = true;
  private int _failures = 0;

  SenderThread(Sender sender) {
    this(sender, SLEEP_INTERVAL_MILLISECONDS);
  }

  @SuppressWarnings("WeakerAccess") /* for testing */
  SenderThread(Sender sender, int sleepInterval) {
    _interval = sleepInterval;
    _sender = sender;
  }

  void terminate() {
    _run = false;
  }

  @Override
  public void run() {

    int index = 1;

    while (_run) {
      try {
        index = _sender.send(index);
        _failures = 0;
      } catch (EventHubException e) {
        if (e.statusCode == 401) {
          Tracker.off();
          return;
        } else {
          _failures++;
        }
      } catch (Throwable ignored) {
        _failures++;
      }

      try {
        int sleepTime = (int) Math.min(Math.pow(2, Math.min(_failures, 10)) * _interval,
            SLEEP_MAX_INTERVAL_MILLISECONDS);
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) { /* continue looping if sleep is interrupted */ }
    }
  }
}
