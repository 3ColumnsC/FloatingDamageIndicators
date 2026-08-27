package com.threecolumnsstudio.floatingdamageindicators.server;

import it.unimi.dsi.fastutil.objects.Object2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.world.entity.LivingEntity;

import java.util.UUID;

public class DamageCaptureState {
    private static final int MAX_SIZE = 500;
    private static final long MAX_AGE_TICKS = 100;
    private static final Object2FloatOpenHashMap<UUID> INITIAL_HEALTH = new Object2FloatOpenHashMap<>();
    private static final Object2FloatOpenHashMap<UUID> ACTUAL_DAMAGE = new Object2FloatOpenHashMap<>();
    private static final Object2BooleanOpenHashMap<UUID> WAS_FULL_HEALTH = new Object2BooleanOpenHashMap<>();
    private static final Object2LongOpenHashMap<UUID> TIMESTAMPS = new Object2LongOpenHashMap<>();

    static {
        INITIAL_HEALTH.defaultReturnValue(Float.NaN);
        ACTUAL_DAMAGE.defaultReturnValue(Float.NaN);
        WAS_FULL_HEALTH.defaultReturnValue(false);
        TIMESTAMPS.defaultReturnValue(-1L);
    }

    public static void recordHealth(LivingEntity target, long gameTime) {
        float total = target.getHealth() + target.getAbsorptionAmount();
        putInitialHealth(target.getUUID(), total, total >= target.getMaxHealth() - 0.01f, gameTime);
    }

    public static void captureDamage(LivingEntity target, long gameTime) {
        float initial = removeInitialHealth(target.getUUID());
        if (!Float.isNaN(initial)) {
            float total = target.getHealth() + target.getAbsorptionAmount();
            putActualDamage(target.getUUID(), Math.max(0, initial - total), gameTime);
        }
    }

    public static void putInitialHealth(UUID id, float health, boolean wasFullHealth, long gameTime) {
        synchronized (INITIAL_HEALTH) {
            INITIAL_HEALTH.put(id, health);
            WAS_FULL_HEALTH.put(id, wasFullHealth);
            TIMESTAMPS.put(id, gameTime);
        }
    }

    public static float removeInitialHealth(UUID id) {
        synchronized (INITIAL_HEALTH) {
            TIMESTAMPS.removeLong(id);
            return INITIAL_HEALTH.removeFloat(id);
        }
    }

    public static void putActualDamage(UUID id, float damage, long gameTime) {
        synchronized (ACTUAL_DAMAGE) {
            ACTUAL_DAMAGE.put(id, damage);
            TIMESTAMPS.put(id, gameTime);
            if (TIMESTAMPS.size() > MAX_SIZE) {
                cleanup(gameTime);
            }
        }
    }

    public static float removeActualDamage(UUID id) {
        synchronized (ACTUAL_DAMAGE) {
            return ACTUAL_DAMAGE.removeFloat(id);
        }
    }

    public static boolean consumeWasFullHealth(UUID id) {
        synchronized (WAS_FULL_HEALTH) {
            return WAS_FULL_HEALTH.removeBoolean(id);
        }
    }

    private static void cleanup(long gameTime) {
        long threshold = gameTime - MAX_AGE_TICKS;
        TIMESTAMPS.object2LongEntrySet().removeIf(entry -> {
            if (entry.getLongValue() < threshold) {
                UUID id = entry.getKey();
                INITIAL_HEALTH.removeFloat(id);
                ACTUAL_DAMAGE.removeFloat(id);
                WAS_FULL_HEALTH.removeBoolean(id);
                return true;
            }
            return false;
        });
    }
}