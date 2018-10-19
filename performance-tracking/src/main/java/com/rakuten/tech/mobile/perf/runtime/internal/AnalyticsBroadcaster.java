package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.rakuten.tech.mobile.perf.core.Analytics;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/** Broadcaster that sends events via local broadcast to peer analytics sdk. */
class AnalyticsBroadcaster implements Analytics {
  private static final String ACTION = "TBD"; // TODO: spec not clear yet

  /**
   * Send event data to peer analytics module. If the app does not bundle a compatible analytics
   * module the broadcast will still be sent, but not processed.
   *
   * @param ctx Context used to get instance of {@link LocalBroadcastManager}
   * @param name Name of the event, may not be null or empty
   * @param data map of event data, may be nested with further maps for deeper
   */
  @SuppressWarnings("unchecked")
  private static void sendEvent(
      @NonNull Context ctx, @NonNull String name, @Nullable Map<String, ?> data) {
    if (TextUtils.isEmpty(name)) return;

    HashMap<String, ?> serializableData;
    if (data instanceof HashMap) {
      serializableData = (HashMap<String, ?>) data;
    } else if (data != null) {
      serializableData = new HashMap<>(data);
    } else {
      serializableData = null;
    }

    Intent intent = new Intent(ACTION);
    intent.putExtra("event-name", name);
    intent.putExtra("event-data", serializableData);

    LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
  }

  private WeakReference<Context> context;

  /**
   * Construct instance of AnalyticsBroadcaster that holds a {@link WeakReference} to the context.
   *
   * @param context Context that will be used for broadcasting
   */
  AnalyticsBroadcaster(@NonNull Context context) {
    this.context = new WeakReference<>(context);
  }

  /**
   * If the context has not been garbage collected this will send an analytics event broadcast.
   * Otherwise nothing happens.
   *
   * @param name Name of the event, may not be null or empty
   * @param data map of event data, may be nested with further maps for deeper
   */
  @Override
  public void sendEvent(@NonNull String name, @Nullable Map<String, ?> data) {
    Context ctx = this.context.get();
    if (ctx != null) AnalyticsBroadcaster.sendEvent(ctx, name, data);
  }
}
