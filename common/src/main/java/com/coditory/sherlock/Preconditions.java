package com.coditory.sherlock;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Preconditions for sherlock distributed lock. Throws {@link IllegalArgumentException} if
 * precondition is not met.
 */
final class Preconditions {
  private Preconditions() {
    throw new IllegalStateException("Do not instantiate utility class");
  }

  @SafeVarargs
  static <T> T expectAll(T value, String message, BiFunction<T, String, T>... expects) {
    for (BiFunction<T, String, T> expect : expects) {
      expect.apply(value, message);
    }
    return value;
  }

  static <T> T expectNonNull(T value) {
    return expectNonNull(value, "Expected non null value");
  }

  static <T> T expectNonNull(T value, String message) {
    if (value == null) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  static String expectNonEmpty(String value) {
    return expectNonEmpty(value, "Expected non empty string. Got: " + value);
  }

  static String expectNonEmpty(String value, String message) {
    if (value == null || value.trim().isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return value;
  }

  static <E> List<E> expectNonEmpty(List<E> list) {
    return expectNonEmpty(list, "Expected non empty list. Got: " + list);
  }

  static <E> List<E> expectNonEmpty(List<E> list, String message) {
    if (list == null || list.isEmpty()) {
      throw new IllegalArgumentException(message);
    }
    return list;
  }

  static Duration expectTruncatedToMillis(Duration duration) {
    return expectTruncatedToMillis(
      duration, "Expected duration truncated to millis. Got: " + duration);
  }

  static Duration expectTruncatedToMillis(Duration duration, String message) {
    Duration truncated = duration.truncatedTo(ChronoUnit.MILLIS);
    if (duration != truncated) {
      throw new IllegalArgumentException(message);
    }
    return duration;
  }
}
