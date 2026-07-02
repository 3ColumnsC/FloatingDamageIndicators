package com.threecolumnsstudio.floatingdamageindicators;

public final class DamageClassifier {
    private DamageClassifier() {}

    public static int getColor(DamageType type) {
        return switch (type) {
            case CRITICAL -> 0xFFFFD700;
            case PROJECTILE -> 0xFF00FFFF;
            case FIRE -> 0xFFFF6600;
            case POISON -> 0xFF4A9E2F;
            case WITHER -> 0xFF3C3C3C;
            case RECEIVING -> 0xFFAAAAAA;
            case NORMAL -> 0xFFFF3333;
        };
    }

    public static String getPrefix(DamageType type) {
        if (type == DamageType.CRITICAL) return "\u2726";
        if (type == DamageType.PROJECTILE) return "\u27B5";
        if (type == DamageType.WITHER) return "\u2620";
        if (type == DamageType.FIRE) return "\u2668";
        if (type == DamageType.POISON) return "\u2697";
        if (type == DamageType.RECEIVING) return "(You) ";
        return "";
    }
}
