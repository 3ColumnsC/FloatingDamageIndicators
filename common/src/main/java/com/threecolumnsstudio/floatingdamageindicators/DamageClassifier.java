package com.threecolumnsstudio.floatingdamageindicators;

public final class DamageClassifier {
    private DamageClassifier() {}

    public static int getColor(DamageType type) {
        return switch (type) {
            case CRITICAL -> 0xFFD700;
            case PROJECTILE -> 0x00FFFF;
            case FIRE -> 0xFF6600;
            case POISON -> 0x4A9E2F;
            case NORMAL -> 0xFF3333;
        };
    }

    public static String getPrefix(DamageType type) {
        if (type == DamageType.CRITICAL) return "\u2726";
        return "";
    }
}
