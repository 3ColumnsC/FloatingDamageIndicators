package com.threecolumnsstudio.floatingdamageindicators.util;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

public class DamageCaptureState {
    private static final Int2FloatOpenHashMap INITIAL_HEALTH = new Int2FloatOpenHashMap();
    private static final Int2FloatOpenHashMap ACTUAL_DAMAGE = new Int2FloatOpenHashMap();

    static {
        INITIAL_HEALTH.defaultReturnValue(Float.NaN);
        ACTUAL_DAMAGE.defaultReturnValue(Float.NaN);
    }

    public static void putInitialHealth(int id, float health) {
        synchronized (INITIAL_HEALTH) {
            INITIAL_HEALTH.put(id, health);
        }
    }

    public static float removeInitialHealth(int id) {
        synchronized (INITIAL_HEALTH) {
            return INITIAL_HEALTH.remove(id);
        }
    }

    public static void putActualDamage(int id, float damage) {
        synchronized (ACTUAL_DAMAGE) {
            ACTUAL_DAMAGE.put(id, damage);
        }
    }

    public static float removeActualDamage(int id) {
        synchronized (ACTUAL_DAMAGE) {
            return ACTUAL_DAMAGE.remove(id);
        }
    }
}
