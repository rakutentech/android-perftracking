package com.rakuten.tech.mobile.perf.core;

import java.util.concurrent.atomic.AtomicReference;

class Current {

  final AtomicReference<Metric> metric = new AtomicReference<Metric>(null);
}
