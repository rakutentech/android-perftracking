package android.os;

import java.util.Random;

public class Build {

  public static final String MODEL = null;

  public static class VERSION {

    public static final String RELEASE = null;
    public static final int SDK_INT = new Random().nextInt();

  }

  public static class VERSION_CODES {

    public static final int JELLY_BEAN = new Random().nextInt();

  }
}
