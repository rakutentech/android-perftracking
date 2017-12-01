package com.rakuten.tech.mobile.perf.runtime.internal;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.gson.Gson;
import com.rakuten.tech.mobile.perf.R;
import com.rakuten.tech.mobile.perf.core.Tracker;
import java.util.Random;

/**
 * ConfigStore - Handles requesting config, response caching and publishing to observers.
 * Can be subscribed to config changes like below,
 * <pre>
 *     <code>
 *         ConfigStore store = //...
 *         store.getObservable().addObserver(new Observer() {
 *            {@literal @}Override
 *             public void update(Observable observable, Object value) {
 *                  if (value instanceof ConfigurationResult) {
 *                      // use new value
 *                  }
 *             }
 *         });
 *     </code>
 * </pre>
 * Config is requested on every launch of the app i.e on starting at construction time and then hourly.
 * If config is already cached, while creating ConfigStore instance store will emit cached config else no config will be emitted via its observable.
 */
class ConfigStore extends Store<ConfigurationResult> {

  private final static String TAG = ConfigStore.class.getSimpleName();
  private static final String PREFS = "app_performance";
  private static final String CONFIG_KEY = "config_key";
  private static final int TIME_INTERVAL = 60 * 60 * 1000; // 1 HOUR in milli seconds

  @Nullable private final String subscriptionKey;
  @Nullable private final String urlPrefix;
  @NonNull private final String appId;
  @NonNull private final RequestQueue requestQueue;
  @NonNull private final String packageName;
  @NonNull private final PackageManager packageManager;
  @NonNull private final SharedPreferences prefs;
  @NonNull private final Resources res;
  @NonNull private final Handler handler;

  ConfigStore(@NonNull Context context, @NonNull RequestQueue requestQueue,
      @NonNull String relayAppId, @Nullable String subscriptionKey, @Nullable String urlPrefix) {
    this.packageManager = context.getPackageManager();
    this.packageName = context.getPackageName();
    this.appId = relayAppId;
    this.res = context.getResources();
    this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    this.requestQueue = requestQueue;
    this.subscriptionKey = subscriptionKey;
    this.urlPrefix = urlPrefix;
    getObservable().publish(readConfigFromCache());
    handler = new Handler(Looper.getMainLooper());
    loadConfigurationFromApi();
    handler.postDelayed(periodicCheck, TIME_INTERVAL);
  }

  @SuppressWarnings("FieldCanBeLocal")
  private final Runnable periodicCheck = new Runnable() {
    public void run() {
      if (Tracker.isTrackerRunning()) {
        handler.postDelayed(this, TIME_INTERVAL);
        loadConfigurationFromApi();
      }
    }
  };

  private void loadConfigurationFromApi() {
    ConfigurationParam param = null;
    try {
      param = new ConfigurationParam.Builder()
          .setAppId(appId)
          .setAppVersion(packageManager.getPackageInfo(packageName, 0).versionName)
          .setCountryCode(res.getConfiguration().locale.getCountry())
          .setPlatform("android")
          .setSdkVersion(res.getString(R.string.perftracking__version))
          .build();
    } catch (PackageManager.NameNotFoundException e) {
      Log.d(TAG, "Error building request to config API", e);
    }

    if (subscriptionKey == null) {
      Log.d(TAG, "Cannot read metadata `com.rakuten.tech.mobile.perf.SubscriptionKey` from" +
          "manifest, automated performance tracking will not work.");
    }
    if (param != null) {
      new ConfigurationRequest(urlPrefix,
          subscriptionKey,
          param, new Response.Listener<ConfigurationResult>() {
        @Override
        public void onResponse(ConfigurationResult newConfig) {
          if (newConfig == null && Tracker.isTrackerRunning()) {
            TrackingManager.deinitialize();
          }

          ConfigurationResult prevConfig = readConfigFromCache();
          boolean shouldRollDice =
              (newConfig != null && Tracker.isTrackerRunning() && prevConfig == null)
                  || (prevConfig != null && newConfig != null
                  && newConfig.getEnablePercent() < prevConfig.getEnablePercent());

          if (shouldRollDice) {
            double randomNumber = new Random(System.currentTimeMillis()).nextDouble() * 100.0;
            if (randomNumber > newConfig.getEnablePercent()) {
              // DeInitialize Tracking Manager
              TrackingManager.deinitialize();
            }
          }
          writeConfigToCache(newConfig);
          getObservable().publish(newConfig);
        }
      }, new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
          // DeInitialize Tracking Manager as we couldn't able to get new config from api
          TrackingManager.deinitialize();
          Log.d(TAG, "Error loading configuration", error);
        }
      }).queue(requestQueue);
    }
  }

  private void writeConfigToCache(ConfigurationResult result) {
    prefs.edit().putString(CONFIG_KEY, new Gson().toJson(result)).apply();
  }

  @Nullable
  private ConfigurationResult readConfigFromCache() {
    String result = prefs.getString(CONFIG_KEY, null);
    return result != null ? new Gson().fromJson(result, ConfigurationResult.class) : null;
  }
}