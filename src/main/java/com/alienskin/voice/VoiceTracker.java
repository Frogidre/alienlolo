package com.alienskin.voice;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class VoiceTracker {
    private static final Map<UUID, Long> LAST_VOICE = new ConcurrentHashMap<>();
    private static final long HOLD_MS = 220;   // still "talking" this long after the last audio chunk

    private VoiceTracker() {}

    public static void mark(UUID id) {
        if (id != null) LAST_VOICE.put(id, System.currentTimeMillis());
    }

    public static boolean isTalking(UUID id) {
        Long t = LAST_VOICE.get(id);
        return t != null && System.currentTimeMillis() - t < HOLD_MS;
    }

    /** 0..1 jaw opening. Flaps while the player is talking, 0 when silent. */
    public static float mouth(UUID id) {
        if (!isTalking(id)) return 0f;
        double t = System.currentTimeMillis() / 85.0;
        double v = Math.abs(Math.sin(t)) * (0.65 + 0.35 * Math.sin(t * 0.37));
        return (float) (0.3 + 0.7 * Math.min(1.0, Math.max(0.0, v)));
    }
}
