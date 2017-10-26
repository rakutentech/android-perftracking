package com.rakuten.tech.mobile.perf.runtime.internal;

import com.rakuten.tech.mobile.perf.core.CachingObservable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(Store.class)
public class StoreShadow<T> {

  public static Object cachedContent;

  @SuppressWarnings("unchecked")
  @Implementation
  public CachingObservable<T> getObservable() {
    return new CachingObservable<>((T) cachedContent);
  }
}