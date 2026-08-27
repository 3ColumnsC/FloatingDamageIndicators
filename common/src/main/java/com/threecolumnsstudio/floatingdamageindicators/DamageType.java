package com.threecolumnsstudio.floatingdamageindicators;

public enum DamageType {
    NORMAL(0xFF3333, "", true),
    CRITICAL(0xFFD700, "\u2726", true),
    PROJECTILE(0x00FFFF, "\u27B5", true),
    FIRE(0xFF6600, "\u2668", true),
    POISON(0x4A9E2F, "\u2697", true),
    WITHER(0x3C3C3C, "\u2620", true),
    RECEIVING(0xAAAAAA, "(You) ", true),
    INSTANT_KILL(0xFFD700, "\u26A1 INSTANT KILL", false);

    private final int defaultColor;
    private final String defaultPrefix;
    private final boolean defaultShowDamage;

    DamageType(int defaultColor, String defaultPrefix, boolean defaultShowDamage) {
        this.defaultColor = defaultColor;
        this.defaultPrefix = defaultPrefix;
        this.defaultShowDamage = defaultShowDamage;
    }

    public int defaultColor() {
        return defaultColor;
    }

    public String defaultPrefix() {
        return defaultPrefix;
    }

    public boolean defaultShowDamage() {
        return defaultShowDamage;
    }
}
