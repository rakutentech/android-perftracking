package com.rakuten.tech.mobile.perf.runtime.internal;


import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.rakuten.tech.mobile.perf.core.Analytics;
import com.rakuten.tech.mobile.perf.runtime.RobolectricUnitSpec;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RuntimeEnvironment;

public class AnalyticsBroadcastSpec extends RobolectricUnitSpec {
  @Mock
  BroadcastReceiver receiver;
  private Context ctx;
  private Analytics analytics;

  @Before
  public void setup() {
    ctx = RuntimeEnvironment.application;
    LocalBroadcastManager.getInstance(ctx).registerReceiver(receiver, new IntentFilter(AnalyticsBroadcaster.ACTION));
    analytics = new AnalyticsBroadcaster(ctx, true);
  }

  @After
  public void cleanup() {
    LocalBroadcastManager.getInstance(ctx).unregisterReceiver(receiver);
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldBroadcastValidNameAndData() {
    Map<String, Object> map = new HashMap<>();
    map.put("key1", "val");
    map.put("key2", 1);

    analytics.sendEvent("name", map);

    Intent intent = captureIntent();
    assertThat(intent).isNotNull();
    assertThat(intent.getStringExtra("event-name")).isEqualTo("rat.name");
    assertThat((Map<String, Object>) intent.getSerializableExtra("event-data")).containsAllEntriesOf(map);
  }

  @Test public void shouldNotBroadcastEventWithoutName() {
    analytics.sendEvent(null, null);

    verifyZeroInteractions(receiver);
  }

  @Test public void shouldBroadcastEventWithoutData() {

    analytics.sendEvent("name", null);

    Intent intent = captureIntent();
    assertThat(intent).isNotNull();
    assertThat(intent.getStringExtra("event-name")).isEqualTo("rat.name");
    assertThat(intent.getExtras().get("event-data")).isNull();
  }

  @SuppressWarnings("unchecked")
  @Test public void shouldBroadcastEventWithoutDifferentMapTypeData() {
    Map<String, Object> map = new TreeMap<>();
    map.put("key1", "val");
    map.put("key2", 1);

    analytics.sendEvent("name", map);

    Intent intent = captureIntent();
    assertThat(intent).isNotNull();
    assertThat((Map<String, Object>) intent.getSerializableExtra("event-data")).containsAllEntriesOf(map);
  }

  @Test public void shouldBlacklistEventsByDomain() {
    AnalyticsBroadcaster analytics = new AnalyticsBroadcaster(ctx, true, "https://rat.rakuten.co.jp/");

    assertThat(analytics.isUrlBlacklisted("http://rat.rakuten.co.jp")).isTrue(); // same schema & host
    assertThat(analytics.isUrlBlacklisted("https://rat.rakuten.co.jp")).isTrue(); // different schema & same host
    assertThat(analytics.isUrlBlacklisted("http://rat.rakuten.co.jp/some/path")).isTrue(); // with url path
    assertThat(analytics.isUrlBlacklisted("")).isFalse(); // invalid url
    assertThat(analytics.isUrlBlacklisted("http://rakuten.co.jp")).isFalse(); // different domain
    assertThat(analytics.isUrlBlacklisted("http://sub.rat.rakuten.co.jp")).isFalse(); // subdomain
    assertThat(analytics.isUrlBlacklisted("rat.rakuten.co.jp")).isFalse(); // missing schema
  }

  @Test public void shouldNotSendEventsWhenDisabled() {
    AnalyticsBroadcaster analytics = new AnalyticsBroadcaster(ctx, false, "https://rat.rakuten.co.jp/");

    analytics.sendEvent("name", null);

    verify(receiver, never()).onReceive(any(Context.class), any(Intent.class));
  }

  private Intent captureIntent() {
    ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);
    verify(receiver, atLeastOnce()).onReceive(any(Context.class), captor.capture());
    return captor.getValue();
  }
}