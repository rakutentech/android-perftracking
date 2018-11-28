package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.text.TextUtils;
import android.util.Log;
import com.rakuten.tech.mobile.perf.R;
import com.rakuten.tech.mobile.perf.core.TelephonyUtil;
import com.rakuten.tech.mobile.perf.core.Tracker;
import java.util.Random;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * ConfigStore - Handles requesting config, response caching and publishing to observers. Can be
 * subscribed to config changes like below,
 *
 * <pre>
 *     <code>
 *         ConfigStore store = //...
 *         store.getObservable().addObserver(new Observer() {
 *            {@literal @}Override
 *             public void update(Observable observable, Object value) {
 *                  if (value instanceof ConfigurationResponse) {
 *                      // use new value
 *                  }
 *             }
 *         });
 *     </code>
 * </pre>
 *
 * Config is requested on every launch of the app i.e on starting at construction time and then
 * hourly. If config is already cached, while creating ConfigStore instance store will emit cached
 * config else no config will be emitted via its observable.
 */
class ConfigStore extends Store<ConfigurationResponse> {

  private static final String TAG = ConfigStore.class.getSimpleName();
  private static final String PREFS = "app_performance";
  private static final String CONFIG_KEY = "config_key";
  private static final int TIME_INTERVAL = 60 * 60 * 1000; // 1 HOUR in milli seconds

  @Nullable private final String subscriptionKey;
  @NonNull private final String appId;
  @NonNull private final String packageName;
  @NonNull private final PackageManager packageManager;
  @NonNull private final SharedPreferences prefs;
  @NonNull private final Resources res;
  @NonNull private final Handler handler;
  @NonNull private final Context context;
  @NonNull private final ConfigApi api;

  ConfigStore(
      @NonNull Context context,
      @NonNull String relayAppId,
      @Nullable String subscriptionKey,
      @NonNull String apiUrlPrefix
  ) {
    this(
        context, relayAppId, subscriptionKey,
        new Retrofit.Builder()
            .baseUrl(apiUrlPrefix)
            .addConverterFactory(new ConverterFactory())
            .build()
            .create(ConfigApi.class)
    );
  }

  @VisibleForTesting
  ConfigStore(
      @NonNull Context context,
      @NonNull String relayAppId,
      @Nullable String subscriptionKey,
      @NonNull ConfigApi api) {
    this.context = context;
    this.packageManager = context.getPackageManager();
    this.packageName = context.getPackageName();
    this.appId = relayAppId;
    this.res = context.getResources();
    this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    this.subscriptionKey = subscriptionKey;
    this.api = api;
    getObservable().publish(readConfigFromCache());
    handler = new Handler(Looper.getMainLooper());
    loadConfigurationFromApi();
    handler.postDelayed(periodicCheck, TIME_INTERVAL);
  }

  @SuppressWarnings("FieldCanBeLocal")
  private final Runnable periodicCheck =
      new Runnable() {
        public void run() {
          if (Tracker.isTrackerRunning()) {
            handler.postDelayed(this, TIME_INTERVAL);
            loadConfigurationFromApi();
          }
        }
      };

  private void loadConfigurationFromApi() {
    try {
      String appVersion = packageManager.getPackageInfo(packageName, 0).versionName;
      String sdkVersion = res.getString(R.string.perftracking__version);
      String countryCode = TelephonyUtil.getCountryCode(context);
      String osVersion = VERSION.RELEASE;
      String device = Build.MODEL;
      validateParams(appId, appVersion, sdkVersion, countryCode, osVersion, device);

      api.config(appId, appVersion, subscriptionKey, sdkVersion, countryCode, osVersion, device)
          .enqueue(new Callback<ConfigurationResponse>() {
            @Override
            public void onResponse(Call<ConfigurationResponse> call,
                Response<ConfigurationResponse> response) {
              if (response.isSuccessful()) {
                handleNewConfig(response.body());
              } else {
                TrackingManager.deinitialize();
                Log.d(TAG, "Failed to load configuration with HTTP status " + response.code());
              }
            }

            @Override
            public void onFailure(Call<ConfigurationResponse> call, Throwable t) {
              // DeInitialize Tracking Manager as we couldn't able to get new config from api
              TrackingManager.deinitialize();
              Log.d(TAG, "Error loading configuration", t);
            }
          });

    } catch (PackageManager.NameNotFoundException e) {
      Log.d(TAG, "Error building request to config API", e);
    }
  }

  private void handleNewConfig(@Nullable ConfigurationResponse newConfig) {
    if (newConfig == null && Tracker.isTrackerRunning()) {
      TrackingManager.deinitialize();
    }

    ConfigurationResponse prevConfig = readConfigFromCache();
    boolean shouldRollDice =
        (newConfig != null && Tracker.isTrackerRunning() && prevConfig == null)
            || (prevConfig != null
            && newConfig != null
            && newConfig.getEnablePercent() < prevConfig.getEnablePercent());

    if (shouldRollDice) {
      double randomNumber =
          new Random(System.currentTimeMillis()).nextDouble() * 100.0;
      if (randomNumber > newConfig.getEnablePercent()) {
        // DeInitialize Tracking Manager
        TrackingManager.deinitialize();
      }
    }
    if(newConfig != null) {
      prefs.edit().putString(CONFIG_KEY, newConfig.toString()).apply();
    } else {
      prefs.edit().remove(CONFIG_KEY).apply();
    }
    getObservable().publish(newConfig);
  }

  @Nullable
  private ConfigurationResponse readConfigFromCache() {
    String result = prefs.getString(CONFIG_KEY, null);
    return result != null ? new ConfigurationResponse(result) : null;
  }

  private static void validateParams(String appId, String appVersion,
      String sdkVersion, String countryCode, String osVersion, String device) {
    if (TextUtils.isEmpty(appId)) {
      throw new IllegalStateException(
          "App Id cannot be null or empty, Please set proper metadata `com.rakuten.tech.mobile.relay.AppId` in manifest");
    }
    if (appVersion == null) {
      throw new IllegalStateException("App Version cannot be null");
    }
    if (sdkVersion == null) {
      throw new IllegalStateException("Sdk Version cannot be null");
    }
    if (countryCode == null) {
      throw new IllegalStateException("Country Code cannot be null");
    }
    if (osVersion == null) {
      throw new IllegalStateException("OS Version cannot be null");
    }
    if (device == null) {
      throw new IllegalStateException("Device cannot be null");
    }
  }
}
