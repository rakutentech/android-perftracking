package com.rakuten.tech.mobile.perf.runtime.shadow;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.rakuten.tech.mobile.perf.runtime.MockedQueue;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(RequestQueue.class)
public class RequestQueueShadow {

  public static MockedQueue queue = new MockedQueue();

  @Implementation public void start() {
  }

  @Implementation public <T> Request<T> add(Request<T> request) {
    return queue.add(request);
  }
}
