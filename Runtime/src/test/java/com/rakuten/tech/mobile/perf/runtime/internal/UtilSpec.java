package com.rakuten.tech.mobile.perf.runtime.internal;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import com.rakuten.tech.mobile.perf.runtime.TestRawData;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;

public class UtilSpec extends RobolectricUnitSpec {

  @Mock
  PackageManager packageManager;
  @Rule
  public TestRawData releaseSignRaw = new TestRawData("signature-release.bin");
  @Rule
  public TestRawData debugSignRaw = new TestRawData("signature-debug.bin");
  Context context;
  PackageInfo packageInfo = new PackageInfo();

  @Before
  public void init() throws PackageManager.NameNotFoundException {
    context = spy(RuntimeEnvironment.application);
    when(context.getPackageManager()).thenReturn(packageManager);
    when(packageManager.getPackageInfo(anyString(), anyInt())).thenReturn(packageInfo);
    packageInfo.signatures = null;
  }

  @Test
  public void shouldReturnTrueIfSignatureIsForDebug() throws PackageManager.NameNotFoundException {
    Signature[] signatures = {new Signature(debugSignRaw.data)};
    packageInfo.signatures = signatures;

    assertThat(Util.isAppDebuggable(context)).isTrue();
  }

  @Test
  public void shouldReturnFalseIfSignatureIsForRelease()
      throws PackageManager.NameNotFoundException {
    Signature[] signatures = {new Signature(releaseSignRaw.data)};
    packageInfo.signatures = signatures;

    assertThat(Util.isAppDebuggable(context)).isFalse();
  }

  @Test
  public void shouldReturnFalseIfSignatureIsMissing()
      throws PackageManager.NameNotFoundException {

    assertThat(Util.isAppDebuggable(context)).isFalse();
  }
}
