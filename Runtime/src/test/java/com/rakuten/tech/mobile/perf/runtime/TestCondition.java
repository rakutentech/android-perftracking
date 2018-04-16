package com.rakuten.tech.mobile.perf.runtime;

import org.assertj.core.api.Condition;

import java.util.Map;

public class TestCondition {

  public static Condition<Map<String, String>> keyValue(final String key, final String value) {
    return new Condition<Map<String, String>>() {
      @Override
      public boolean matches(Map<String, String> map) {
        return map.keySet().contains(key) &&
            ((value == null && map.get(key) == null) ||
                    (value != null && value.equals(map.get(key))));
      }
    };
  }
}
