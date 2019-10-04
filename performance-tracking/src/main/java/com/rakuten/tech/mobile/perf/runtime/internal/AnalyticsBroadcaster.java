package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import com.rakuten.tech.mobile.perf.core.Analytics;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Broadcaster that sends events via local broadcast to peer analytics sdk. */
class AnalyticsBroadcaster extends Analytics {
  @VisibleForTesting
  static final String ACTION = "jp.co.rakuten.sdtd.analytics.ExternalEvent";
  private final List<URL> blacklist;
  private final boolean enabled;

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
    intent.putExtra("event-name", "rat." + name);
    intent.putExtra("event-data", serializableData);

    LocalBroadcastManager.getInstance(ctx).sendBroadcast(intent);
  }

  private WeakReference<Context> context;

  /**
   * Construct instance of AnalyticsBroadcaster that holds a {@link WeakReference} to the context.
   *
   * @param context Context that will be used for broadcasting
   * @param blacklist variable list of URL strings that will be used to blacklist broadcasts.
   */
  AnalyticsBroadcaster(@NonNull Context context, boolean enabled, String... blacklist) {
    this.context = new WeakReference<>(context);
    this.enabled = enabled;
    this.blacklist = new ArrayList<>(blacklist.length);
    for (String url : blacklist) {
      try {
        this.blacklist.add(new URL(url));
      } catch (MalformedURLException ignored) {
      }
    }
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
    if (!enabled) {
      return;
    }

    Context ctx = this.context.get();
    if (ctx != null) AnalyticsBroadcaster.sendEvent(ctx, name, data);
  }

  /**
   * Checks if an event is recording an HTTP request to a blacklisted domain.
   *
   * @param url String url of the recorded HTTP request.
   * @return true if event records an HTTP request to a blacklisted domain, false otherwise.
   */
  @Override
  protected boolean isUrlBlacklisted(String url) {
    try {
      URL candidate = new URL(url);
      for (URL blacklisted : blacklist) {
        if (blacklisted.getHost().equals(candidate.getHost())) {
          return true;
        }
      }
    } catch (MalformedURLException ignored) {
    }
    return super.isUrlBlacklisted(url);
  }
}
