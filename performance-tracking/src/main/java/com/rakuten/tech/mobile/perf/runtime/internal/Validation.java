package com.rakuten.tech.mobile.perf.runtime.internal;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import java.util.regex.Pattern;

/**
 * Function(s) to validate user input
 */
public class Validation {

  private static String validInputRegex = "[a-zA-Z0-9 ._-]*";

  /**
   * Validate metric or measurement ids, should match backend validation rules.
   * Current rules are:
   * - not null
   * - not empty string
   * example usage
   * <pre>
   *     if(Validation.isInvalidId(id)) {
   *         throw new IllegalArgumentException("Id is invalid");
   *     }
   * </pre>
   *
   * @param idCandidate id to be checked for validity
   * @return true if invalid, false in valid
   */
  public static boolean isInvalidId(@Nullable String idCandidate) {
    return (TextUtils.isEmpty(idCandidate) || !Pattern.matches(validInputRegex, idCandidate));
  }
}
