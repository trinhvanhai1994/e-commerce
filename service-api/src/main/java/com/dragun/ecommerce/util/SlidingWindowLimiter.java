package com.dragun.ecommerce.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory sliding-window limit per key (IP, phone). Single-instance only, like ReviewSubmissionLimiter:
 * with several API instances each keeps its own window. Move to a shared store if the API is scaled out.
 */
public class SlidingWindowLimiter {

    private static final int MAX_TRACKED_KEYS = 10_000;

    private final int maxPerWindow;
    private final long windowMillis;
    private final Map<String, Deque<Long>> hits = new ConcurrentHashMap<>();

    public SlidingWindowLimiter(int maxPerWindow, long windowMillis) {
        this.maxPerWindow = maxPerWindow;
        this.windowMillis = windowMillis;
    }

    /** Records an attempt and returns false when the key already used its quota in the current window. */
    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        if (hits.size() > MAX_TRACKED_KEYS) {
            hits.entrySet().removeIf(e -> {
                synchronized (e.getValue()) {
                    Long last = e.getValue().peekLast();
                    return last == null || now - last > windowMillis;
                }
            });
        }
        Deque<Long> window = hits.computeIfAbsent(key == null ? "unknown" : key, k -> new ArrayDeque<>());
        synchronized (window) {
            while (!window.isEmpty() && now - window.peekFirst() > windowMillis) {
                window.pollFirst();
            }
            if (window.size() >= maxPerWindow) {
                return false;
            }
            window.addLast(now);
            return true;
        }
    }
}
