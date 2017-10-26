package android.os;

public class Handler {

  public boolean post(Runnable runnable) {
    return false;
  }

  public boolean postAtFrontOfQueue(Runnable runnable) {
    return false;
  }

  public boolean postAtTime(Runnable runnable, long uptimeMillis) {
    return false;
  }

  public boolean postAtTime(Runnable runnable, Object token, long uptimeMillis) {
    return false;
  }

  public boolean postDelayed(Runnable runnable, long delayMillis) {
    return false;
  }
}
