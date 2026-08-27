package com.threecolumnsstudio.floatingdamageindicators.server;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import net.minecraft.world.entity.LivingEntity;

public class DamageCaptureState {
    private static final Int2FloatOpenHashMap INITIAL_HEALTH = new Int2FloatOpenHashMap();
    private static final Int2FloatOpenHashMap ACTUAL_DAMAGE = new Int2FloatOpenHashMap();

    static {
        INITIAL_HEALTH.defaultReturnValue(Float.NaN);
        ACTUAL_DAMAGE.defaultReturnValue(Float.NaN);
    }

    public static void recordHealth(LivingEntity target) {
        putInitialHealth(target.getId(), target.getHealth());
    }

    public static void captureDamage(LivingEntity target) {
        float initial = removeInitialHealth(target.getId());
        if (!Float.isNaN(initial)) {
            putActualDamage(target.getId(), Math.max(0, initial - target.getHealth()));
        }
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