package android.content;

public abstract class Context {

  public static final String TELEPHONY_SERVICE = "telephony";

  public void startActivity(Intent intent) {
  }

  public abstract Object getSystemService(String name);
}
