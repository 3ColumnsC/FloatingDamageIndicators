package com.threecolumnsstudio.floatingdamageindicators;

import java.util.Objects;

public final class DamageClassifier {
    private DamageClassifier() {}

    public static int getColor(DamageType type) {
        Objects.requireNonNull(type);
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
        Objects.requireNonNull(type);
        return switch (type) {
            case CRITICAL -> "\u2726";
            case PROJECTILE -> "\u27B5";
            case WITHER -> "\u2620";
            case FIRE -> "\u2668";
            case POISON -> "\u2697";
            case RECEIVING -> "(You) ";
            case NORMAL -> "";
        };
    }
}
