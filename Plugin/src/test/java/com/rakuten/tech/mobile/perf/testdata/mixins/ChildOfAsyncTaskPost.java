package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.AsyncTask;
import com.rakuten.tech.mobile.perf.core.Tracker;


public class ChildOfAsyncTaskPost<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

  protected Result doInBackground(Params... params) {
    int id = Tracker.startMethod(this, "doInBackground");
    try {
      return this.com_rakuten_tech_mobile_perf_doInBackground(params);
    } finally {
      Tracker.endMethod(id);
    }
  }

  private Result com_rakuten_tech_mobile_perf_doInBackground(Params... params) {
    return null;
  }
}
