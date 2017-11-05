package android.content;

public abstract class Context {

  public static final String TELEPHONY_SERVICE = "TELEPHONY_SERVICE";

  public void startActivity(Intent intent) {
  }

  public abstract Object getSystemService(String name);
}
