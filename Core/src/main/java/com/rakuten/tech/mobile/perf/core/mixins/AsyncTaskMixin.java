package com.rakuten.tech.mobile.perf.core.mixins;

import android.os.AsyncTask;
import com.rakuten.tech.mobile.perf.core.Tracker;
import com.rakuten.tech.mobile.perf.core.annotations.MixSubclassOf;
import com.rakuten.tech.mobile.perf.core.annotations.ReplaceMethod;

@MixSubclassOf(AsyncTask.class)
public class AsyncTaskMixin<Params, Progress, Result> {

  @ReplaceMethod
  protected Result doInBackground(Params... params) {
    int id = Tracker.startMethod(this, "doInBackground");
    try {
      return doInBackground(params);
    } finally {
      Tracker.endMethod(id);
    }
  }
}
