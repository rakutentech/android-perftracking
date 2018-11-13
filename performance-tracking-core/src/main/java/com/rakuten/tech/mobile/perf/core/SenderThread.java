package com.rakuten.tech.mobile.perf.core;

class SenderThread extends Thread {

  private static final int SLEEP_INTERVAL_MILLISECONDS = 10_000;
  private static final int SLEEP_MAX_INTERVAL_MILLISECONDS = 1_800_000;

  private final Sender sender;
  private final int interval;
  private volatile boolean run = true;
  private int failures = 0;

  SenderThread(Sender sender) {
    this(sender, SLEEP_INTERVAL_MILLISECONDS);
  }

  @SuppressWarnings("WeakerAccess") /* for testing */
  SenderThread(Sender sender, int sleepInterval) {
    interval = sleepInterval;
    this.sender = sender;
  }

  void terminate() {
    run = false;
  }

  @Override
  public void run() {

    int index = 1;

    while (run) {
      try {
        index = sender.send(index);
        failures = 0;
      } catch (EventHubException e) {
        if (e.statusCode == 401) {
          Tracker.off();
          return;
        } else {
          failures++;
        }
      } catch (Throwable ignored) {
        failures++;
      }

      try {
        int sleepTime = (int) Math.min(Math.pow(2, Math.min(failures, 10)) * interval,
            SLEEP_MAX_INTERVAL_MILLISECONDS);
        Thread.sleep(sleepTime);
      } catch (InterruptedException e) { /* continue looping if sleep is interrupted */ }
    }
  }
}
