package com.rakuten.tech.mobile.perf.core;

public class Metric {

  static final long MAX_TIME = 10000L; // 10 s

  String id;
  long startTime;
  long endTime;
  int urls;

  /**
   * deep copy values of `this` struct into newly allocated struct
   *
   * @return deep copy of `this`
   */
  Metric copy() {
    Metric copy = new Metric();
    copy.id = id;
    copy.startTime = startTime;
    copy.endTime = endTime;
    copy.urls = urls;
    return copy;
  }
}
