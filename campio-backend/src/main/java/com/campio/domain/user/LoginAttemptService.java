package com.campio.domain.user;

import com.campio.global.exception.TooManyRequestsException;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

@Service
public class LoginAttemptService {

  private static final int MAX_FAILURES = 5;
  private static final Duration WINDOW = Duration.ofMinutes(15);
  private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

  public void check(String key) {
    Attempt attempt = attempts.get(key);
    if (attempt == null) return;
    if (attempt.startedAt.plus(WINDOW).isBefore(Instant.now())) {
      attempts.remove(key, attempt);
      return;
    }
    if (attempt.failures >= MAX_FAILURES) {
      throw new TooManyRequestsException("Too many login attempts. Try again later");
    }
  }

  public void failure(String key) {
    attempts.compute(key, (ignored, current) -> {
      Instant now = Instant.now();
      if (current == null || current.startedAt.plus(WINDOW).isBefore(now)) return new Attempt(1, now);
      return new Attempt(current.failures + 1, current.startedAt);
    });
  }

  public void success(String key) {
    attempts.remove(key);
  }

  private static class Attempt {
    private final int failures;
    private final Instant startedAt;

    private Attempt(int failures, Instant startedAt) {
      this.failures = failures;
      this.startedAt = startedAt;
    }
  }
}
