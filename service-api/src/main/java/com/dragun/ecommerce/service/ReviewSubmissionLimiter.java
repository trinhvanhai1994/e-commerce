package com.dragun.ecommerce.service;

import org.springframework.stereotype.Component;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window limit on public review submissions per client key (IP).
 * Single-instance only: with several API instances each keeps its own window, which still bounds abuse per
 * instance. Good enough for the anonymous review form; move to a shared store if the API is scaled out.
 */
@Component
public class ReviewSubmissionLimiter {

    static final int MAX_PER_WINDOW = 5;
    static final long WINDOW_MILLIS = 10 * 60 * 1000L;
    private static final int MAX_TRACKED_KEYS = 10_000;

    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    /** Records an attempt and returns false when the key already used its quota in the current window. */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        if (hits.size() > MAX_TRACKED_KEYS) {
            evictExpired(now);
        }
        Deque<Long> window = hits.computeIfAbsent(key == null ? "unknown" : key, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MILLIS) {
                window.pollFirst();
            }
            if (window.size() >= MAX_PER_WINDOW) {
                return false;
            }
            window.addLast(now);
            return true;
        }
    }

    private void evictExpired(long now) {
        hits.entrySet().removeIf(e -> {
            synchronized (e.getValue()) {
                Long last = e.getValue().peekLast();
                return last == null || now - last > WINDOW_MILLIS;
            }
        });
    }
}
