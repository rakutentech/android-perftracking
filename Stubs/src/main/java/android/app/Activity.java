package android.app;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.AttributeSet;
import android.view.View;

public class Activity {

  public static final String ACTIVITY_SERVICE = "ACTIVITY_SERVICE";

  public Intent getIntent() {
    return null;
  }

  protected void onCreate(Bundle savedInstanceState) {
  }

  public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
  }

  public View onCreateView(String name, Context context, AttributeSet attrs) {
    return null;
  }

  public View onCreateView(View parent, String name, Context context, AttributeSet attrs) {
    return null;
  }

  protected void onStart() {
  }

  protected void onStop() {
  }

  protected void onResume() {
  }

  protected void onPause() {
  }

  protected void onRestart() {
  }

  protected void onDestroy() {
  }

  public void onBackPressed() {
  }

  public void onUserInteraction() {
  }
}
