package com.rakuten.tech.mobile.perf.runtime.internal;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

/**
 * BatteryInfoStore - Handles registering for battery info updates and publishing to observers.
 * Can be subscribed to battery changes like below,
 * <pre>
 *     <code>
 *         BatteryInfoStore store = //...
 *         store.getObservable().addObserver(new Observer() {
 *            {@literal @}Override
 *             public void update(Observable observable, Object value) {
 *                   // use new value
 *             }
 *         });
 *     </code>
 * </pre>
 *
 */
class BatteryInfoStore extends Store<Float> {

  BatteryInfoStore(Context context) {

    context.registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context c, Intent i) {

        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        float batteryPct = level / (float) scale;
        getObservable().publish(batteryPct);

      }
    }, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

  }

}