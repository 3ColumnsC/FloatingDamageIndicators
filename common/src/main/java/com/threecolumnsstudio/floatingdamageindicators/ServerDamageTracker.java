package com.threecolumnsstudio.floatingdamageindicators;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ServerDamageTracker {

    private static final Map<UUID, Entry> TRACKED = new ConcurrentHashMap<>();
    public static final long MAX_TRACK_TICKS = 600;

    private record Entry(UUID playerUuid, long gameTime) {}

    public static void track(UUID entityUuid, UUID playerUuid, long gameTime) {
        TRACKED.put(entityUuid, new Entry(playerUuid, gameTime));
        if (TRACKED.size() > 200) {
            cleanup(gameTime);
        }
    }

    public static boolean isRecentlyHit(UUID entityUuid, long gameTime) {
        Entry entry = TRACKED.get(entityUuid);
        return entry != null && gameTime - entry.gameTime <= MAX_TRACK_TICKS;
    }

    public static UUID getTrackingPlayer(UUID entityUuid, long gameTime) {
        Entry entry = TRACKED.get(entityUuid);
        if (entry != null && gameTime - entry.gameTime <= MAX_TRACK_TICKS) {
            return entry.playerUuid;
        }
        return null;
    }

    private static void cleanup(long gameTime) {
        TRACKED.values().removeIf(e -> gameTime - e.gameTime > MAX_TRACK_TICKS);
    }
}
