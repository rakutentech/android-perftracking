package com.rakuten.tech.mobile.perf.testdata.mixins;

public class ChildOfThread extends Thread {

  @Override public void run() {
    super.run();
  }
}
