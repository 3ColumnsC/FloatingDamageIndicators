package com.threecolumnsstudio.floatingdamageindicators.util;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;

public class DamageCaptureState {
    private static final int MAX_SIZE = 500;
    private static final long MAX_AGE_TICKS = 100;
    private static final Int2FloatOpenHashMap INITIAL_HEALTH = new Int2FloatOpenHashMap();
    private static final Int2FloatOpenHashMap ACTUAL_DAMAGE = new Int2FloatOpenHashMap();
    private static final Int2LongOpenHashMap TIMESTAMPS = new Int2LongOpenHashMap();

    static {
        INITIAL_HEALTH.defaultReturnValue(Float.NaN);
        ACTUAL_DAMAGE.defaultReturnValue(Float.NaN);
        TIMESTAMPS.defaultReturnValue(-1L);
    }

    public static void putInitialHealth(int id, float health, long gameTime) {
        synchronized (INITIAL_HEALTH) {
            INITIAL_HEALTH.put(id, health);
            TIMESTAMPS.put(id, gameTime);
        }
    }

    public static float removeInitialHealth(int id) {
        synchronized (INITIAL_HEALTH) {
            TIMESTAMPS.remove(id);
            return INITIAL_HEALTH.remove(id);
        }
    }

    public static void putActualDamage(int id, float damage, long gameTime) {
        synchronized (ACTUAL_DAMAGE) {
            ACTUAL_DAMAGE.put(id, damage);
            TIMESTAMPS.put(id, gameTime);
            if (TIMESTAMPS.size() > MAX_SIZE) {
                cleanup(gameTime);
            }
        }
    }

    public static float removeActualDamage(int id) {
        synchronized (ACTUAL_DAMAGE) {
            return ACTUAL_DAMAGE.remove(id);
        }
    }

    private static void cleanup(long gameTime) {
        long threshold = gameTime - MAX_AGE_TICKS;
        TIMESTAMPS.int2LongEntrySet().removeIf(entry -> {
            if (entry.getLongValue() < threshold) {
                int id = entry.getIntKey();
                INITIAL_HEALTH.remove(id);
                ACTUAL_DAMAGE.remove(id);
                return true;
            }
            return false;
        });
    }
}
