package com.rakuten.tech.mobile.perf.runtime.internal;

import android.support.annotation.NonNull;
import com.rakuten.tech.mobile.perf.core.CachingObservable;

/** Store - used to create and return an observable with given type T. */
class Store<T> {

  @NonNull private final CachingObservable<T> observable;

  Store() {
    observable = new CachingObservable<>(null);
  }

  @NonNull
  CachingObservable<T> getObservable() {
    return observable;
  }
}
