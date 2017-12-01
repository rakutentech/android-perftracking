package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.rakuten.tech.mobile.perf.core.Config;
import com.rakuten.tech.mobile.perf.runtime.Metric;
import java.util.Random;


/**
 * RuntimeContentProvider - a custom high-priority ContentProvider, to start tracking early in the
 * process launch phase.
 */

public class RuntimeContentProvider extends ContentProvider {

  private static final String TAG = RuntimeContentProvider.class.getSimpleName();

  @Override
  public boolean onCreate() {
    Context context = getContext();
    if (context == null) {
      return false;
    }
    if (!AppPerformanceConfig.enabled) {
      return false; // Return when instrumentation is disabled
    }

    RequestQueue queue = new RequestQueue(new NoCache(), new BasicNetwork(new HurlStack()));
    queue.start();

    BatteryInfoStore batteryInfoStore = new BatteryInfoStore(context);

    String subscriptionKey = Util.getSubscriptionKey(context);

    String configUrlPrefix = Util
        .getMeta(context, "com.rakuten.tech.mobile.perf.ConfigurationUrlPrefix");
    String relayAppId = Util.getRelayAppId(context);
    ConfigStore configStore = new ConfigStore(context, queue, relayAppId, subscriptionKey,
        configUrlPrefix);

    // Read last config from cache
    Config config = createConfig(context, configStore.getObservable().getCachedValue(), relayAppId);
    if (config != null) {
      String locationUrlPrefix = Util
          .getMeta(context, "com.rakuten.tech.mobile.perf.LocationUrlPrefix");
      LocationStore locationStore = new LocationStore(context, queue, subscriptionKey,
          locationUrlPrefix);
      // Initialise Tracking Manager
      TrackingManager.initialize(context, config, locationStore.getObservable(),
          batteryInfoStore.getObservable());
      Metric.start("_launch");
    }
    return false;
  }

  /**
   * Configuration for {@link TrackingManager}
   *
   * @param context    application context
   * @param lastConfig cached config, may be null
   * @return Configuration for {@link TrackingManager}, may be null
   */
  @Nullable
  private Config createConfig(@NonNull Context context, @Nullable ConfigurationResult lastConfig,
      @Nullable String appId) {
    PackageManager packageManager = context.getPackageManager();
    String packageName = context.getPackageName();
    if (lastConfig == null) {
      return null;
    }
    Config config = null; // configuration for TrackingManager

    double enablePercent = lastConfig.getEnablePercent();

    double randomNumber = new Random(System.currentTimeMillis()).nextDouble() * 100.0;
    if (randomNumber <= enablePercent) {
      config = new Config();
      config.app = packageName;
      config.relayAppId = appId;
      try {
        config.version = packageManager
            .getPackageInfo(packageName, 0).versionName;
      } catch (PackageManager.NameNotFoundException e) {
        Log.d(TAG, e.getMessage());
      }
      try {
        ApplicationInfo ai = packageManager
            .getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        Bundle bundle = ai.metaData;
        config.debug = bundle.getBoolean("com.rakuten.tech.mobile.perf.debug");
      } catch (PackageManager.NameNotFoundException | NullPointerException e) {
        config.debug = false;
      }
      config.eventHubUrl = lastConfig.getSendUrl();
      config.header = lastConfig.getHeader();
    }

    return config;
  }

  @Nullable
  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection,
      String[] selectionArgs, String sortOrder) {
    return null;
  }

  @Nullable
  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {
    return 0;
  }
}
