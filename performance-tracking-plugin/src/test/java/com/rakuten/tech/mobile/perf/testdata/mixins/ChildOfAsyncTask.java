package com.rakuten.tech.mobile.perf.testdata.mixins;

import android.os.AsyncTask;


public class ChildOfAsyncTask<Params, Progress, Result> extends AsyncTask<Params, Progress, Result> {

  protected Result doInBackground(Params... params) {
    return null;
  }

}
