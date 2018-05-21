package com.rakuten.tech.mobile.perf.core;

import java.io.IOException;

/**
 * Exception that represent an unexpected error code from the EventHub API
 */
class EventHubException extends IOException {

  final int statusCode;

  EventHubException(int statusCode) {
    super("Failed to send event with status " + statusCode);
    this.statusCode = statusCode;
  }
}
