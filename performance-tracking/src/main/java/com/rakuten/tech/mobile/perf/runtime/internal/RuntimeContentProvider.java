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
import android.text.TextUtils;
import android.util.Log;
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
    
    BatteryInfoStore batteryInfoStore = new BatteryInfoStore(context);

    App manifest = new AppManifestConfig(context);
    String appKey = manifest.appKey().isEmpty() ? manifest.relayAppKey() : manifest.appKey();
    String appId = manifest.appId().isEmpty() ? manifest.relayAppId() : manifest.appId();

    if (appKey.isEmpty()) {
      Log.d(TAG, "Cannot read metadata `com.rakuten.tech.mobile.ras.ProjectSubscriptionKey` from"
          + "manifest, automated performance tracking will not work.");
    }

    if (appId.isEmpty()) {
      Log.d(TAG, "Cannot read metadata `com.rakuten.tech.mobile.ras.AppId` from"
          + "manifest, automated performance tracking will not work.");
    }

    ConfigStore configStore = new ConfigStore(context, appId, appKey, manifest.configUrlPrefix());

    // Read last config from cache
    Config config = createConfig(context, configStore.getObservable().getCachedValue(), appId);
    if (config != null) {
      LocationStore locationStore =
          new LocationStore(context, appKey, manifest.locationUrlPrefix());
      // Initialise Tracking Manager
      TrackingManager.initialize(
          context,
          config,
          locationStore.getObservable(),
          batteryInfoStore.getObservable(),
          new AnalyticsBroadcaster(context, config.enableAnalyticsEvents, manifest.ratEndPoint()));
      Metric.start("_launch");
    }
    return false;
  }

  /**
   * Configuration for {@link TrackingManager}.
   *
   * @param context application context
   * @param lastConfig cached config, may be null
   * @return Configuration for {@link TrackingManager}, may be null
   */
  @Nullable
  private Config createConfig(
      @NonNull final Context context,
      @Nullable final ConfigurationResponse lastConfig,
      @Nullable final String appId) {
    if (lastConfig == null) {
      return null;
    }
    PackageManager packageManager = context.getPackageManager();
    String packageName = context.getPackageName();
    Config config = null; // configuration for TrackingManager

    boolean appDebuggable = Util.isAppDebuggable(context);
    double enablePercent = lastConfig.getEnablePercent();
    double randomNumber = new Random(System.currentTimeMillis()).nextDouble() * 100.0;
    boolean enableTracking = randomNumber < enablePercent;
    if (appDebuggable || enableTracking) {
      config = new Config();
      config.app = packageName;
      config.relayAppId = appId;
      try {
        config.version = packageManager.getPackageInfo(packageName, 0).versionName;
      } catch (PackageManager.NameNotFoundException e) {
        Log.d(TAG, e.getMessage());
      }
      try {
        ApplicationInfo ai =
            packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        Bundle bundle = ai.metaData;
        config.debug = bundle.getBoolean("com.rakuten.tech.mobile.perf.debug");
      } catch (PackageManager.NameNotFoundException | NullPointerException e) {
        config.debug = false;
      }
      config.enableNonMetricMeasurement = lastConfig.shouldEnableNonMetricMeasurement();
      config.eventHubUrl = lastConfig.getSendUrl();
      config.header = lastConfig.getHeader();
      config.enablePerfTrackingEvents = lastConfig.shouldSendToPerfTracking();
      config.enableAnalyticsEvents = lastConfig.shouldSendToAnalytics();
    }

    return config;
  }

  @Nullable
  @Override
  public Cursor query(
      @NonNull final Uri uri,
      final String[] projection,
      final String selection,
      final String[] selectionArgs,
      final String sortOrder) {
    return null;
  }

  @Nullable
  @Override
  public String getType(@NonNull final Uri uri) {
    return null;
  }

  @Nullable
  @Override
  public Uri insert(@NonNull final Uri uri, final ContentValues values) {
    return null;
  }

  @Override
  public int delete(@NonNull final Uri uri, final String selection, final String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(
      @NonNull final Uri uri,
      final ContentValues values,
      final String selection,
      final String[] selectionArgs) {
    return 0;
  }
}
