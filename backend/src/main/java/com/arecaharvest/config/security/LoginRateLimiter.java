package com.arecaharvest.config.security;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding rate limiter to protect against brute-force attacks on login.
 * Allows at most MAX_ATTEMPTS failed attempts within LOCKOUT_DURATION_SECONDS.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_SECONDS = 300; // 5 minutes

    private static class Attempt {
        int count;
        Instant firstAttempt;
        Instant lockoutUntil;

        Attempt() {
            this.count = 1;
            this.firstAttempt = Instant.now();
        }
    }

    private final Map<String, Attempt> attemptsCache = new ConcurrentHashMap<>();

    public boolean isBlocked(String key) {
        if (key == null || key.isBlank()) return false;
        Attempt attempt = attemptsCache.get(key.toLowerCase().trim());
        if (attempt == null) return false;

        Instant now = Instant.now();
        if (attempt.lockoutUntil != null) {
            if (now.isBefore(attempt.lockoutUntil)) {
                return true;
            } else {
                // Lockout period has passed, reset
                attemptsCache.remove(key.toLowerCase().trim());
                return false;
            }
        }
        return false;
    }

    public void recordFailedAttempt(String key) {
        if (key == null || key.isBlank()) return;
        String normalized = key.toLowerCase().trim();
        Instant now = Instant.now();

        attemptsCache.compute(normalized, (k, attempt) -> {
            if (attempt == null) {
                return new Attempt();
            }
            // If window exceeded without reaching limit, reset
            if (now.getEpochSecond() - attempt.firstAttempt.getEpochSecond() > LOCKOUT_DURATION_SECONDS) {
                return new Attempt();
            }

            attempt.count++;
            if (attempt.count >= MAX_ATTEMPTS) {
                attempt.lockoutUntil = now.plusSeconds(LOCKOUT_DURATION_SECONDS);
            }
            return attempt;
        });
    }

    public void recordSuccess(String key) {
        if (key != null && !key.isBlank()) {
            attemptsCache.remove(key.toLowerCase().trim());
        }
    }

    public long getRemainingLockoutSeconds(String key) {
        if (key == null || key.isBlank()) return 0;
        Attempt attempt = attemptsCache.get(key.toLowerCase().trim());
        if (attempt == null || attempt.lockoutUntil == null) return 0;
        long remaining = attempt.lockoutUntil.getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, remaining);
    }
}
