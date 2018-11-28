package com.rakuten.tech.mobile.perf.runtime.internal;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.rakuten.tech.mobile.perf.core.LocationData;
import com.rakuten.tech.mobile.perf.core.Tracker;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * LocationStore - Handles requesting location, response caching and publishing to observers. Can be
 * subscribed to location changes like below,
 *
 * <pre>
 *     <code>
 *         LocationStore store = //...
 *         store.getObservable().addObserver(new Observer() {
 *            {@literal @}Override
 *             public void update(Observable observable, Object value) {
 *                  if (value instanceof LocationData) {
 *                      // use new value
 *                  }
 *             }
 *         });
 *     </code>
 * </pre>
 *
 * Location is requested on every launch of the app i.e on starting at construction time and then
 * hourly. If location is already cached, while creating LocationStore instance store will emit
 * cached location else no location will be emitted via its observable.
 */
class LocationStore extends Store<LocationData> {

  private static final String TAG = LocationStore.class.getSimpleName();
  private static final String PREFS = "app_performance";
  private static final String LOCATION_KEY = "location_key";
  private static final int TIME_INTERVAL = 60 * 60 * 1000; // 1 HOUR in milli seconds

  @Nullable
  private final String subscriptionKey;
  @NonNull
  private final GeoLocationApi api;
  @NonNull
  private final SharedPreferences prefs;
  @NonNull
  private final Handler handler;

  LocationStore(
      @NonNull Context context,
      @Nullable String subscriptionKey,
      @NonNull String urlPrefix) {
    this(context, subscriptionKey,
        new Retrofit.Builder()
            .baseUrl(urlPrefix)
            .addConverterFactory(new ConverterFactory())
            .build()
            .create(GeoLocationApi.class)
    );
  }

  @VisibleForTesting
  LocationStore(
      @NonNull Context context,
      @Nullable String subscriptionKey,
      @NonNull GeoLocationApi api) {
    this.prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    this.subscriptionKey = subscriptionKey;
    this.api = api;
    this.handler = new Handler(Looper.getMainLooper());

    getObservable().publish(readLocationFromCache());
    loadLocationFromApi();
    handler.postDelayed(periodicLocationCheck, TIME_INTERVAL);
  }

  @SuppressWarnings("FieldCanBeLocal")
  private final Runnable periodicLocationCheck =
      new Runnable() {
        public void run() {
          if (Tracker.isTrackerRunning()) {
            handler.postDelayed(this, TIME_INTERVAL);
            loadLocationFromApi();
          }
        }
      };

  private void loadLocationFromApi() {
    if (subscriptionKey == null) {
      Log.d(
          TAG,
          "Cannot read metadata `com.rakuten.tech.mobile.perf.SubscriptionKey` from "
              + "manifest automated performance tracking will not work.");
    }

    api.location(subscriptionKey)
        .enqueue(new Callback<GeoLocationResponse>() {
          @Override
          public void onResponse(Call<GeoLocationResponse> call,
              Response<GeoLocationResponse> response) {
            if(response.isSuccessful()) {
              GeoLocationResponse newLocation = response.body();
              LocationData locationData =
                  new LocationData(newLocation.getCountry(), newLocation.getRegion());
              prefs.edit().putString(LOCATION_KEY, toJsonString(locationData)).apply();
              getObservable().publish(locationData);
            } else {
              Log.d(TAG, "Failed to load location with HTTP status code " + response.code());
            }
          }

          @Override
          public void onFailure(Call<GeoLocationResponse> call, Throwable t) {
            Log.d(TAG, "Error loading location", t);
          }
        });
  }

  @VisibleForTesting
  @NonNull
  static String toJsonString(LocationData result) {
    try {
      return new JSONObject()
          .put("country", result.country)
          .put("region", result.region)
          .toString();
    } catch (JSONException e) {
      return "{}";
    }
  }

  @NonNull
  static private LocationData fromJsonString(@NonNull String json) {
    try {
      JSONObject obj = new JSONObject(json);
      return new LocationData(obj.getString("country"), obj.getString("region"));
    } catch (JSONException e) {
      return new LocationData("", "");
    }
  }

  @Nullable
  private LocationData readLocationFromCache() {
    String result = prefs.getString(LOCATION_KEY, null);
    return result != null ? fromJsonString(result) : null;
  }
}
