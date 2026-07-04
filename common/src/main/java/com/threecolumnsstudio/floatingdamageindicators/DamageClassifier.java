package com.threecolumnsstudio.floatingdamageindicators;

import com.threecolumnsstudio.floatingdamageindicators.ModConfig.FormatEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

public final class DamageClassifier {
    private static final Logger LOGGER = LoggerFactory.getLogger("FDI-Classifier");

    private DamageClassifier() {}

    public static int getColor(DamageType type) {
        Objects.requireNonNull(type);
        FormatEntry fmt = ModConfig.get().getFormat(type);
        if (fmt != null && fmt.color != null && !fmt.color.isEmpty()) {
            try {
                return 0xFF000000 | Integer.parseUnsignedInt(fmt.color, 16);
            } catch (NumberFormatException e) {
                LOGGER.warn("Invalid color '{}' for damage type {}, using default", fmt.color, type);
            }
        }
        return switch (type) {
            case INSTANT_KILL -> 0xFFFFD700;
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
        FormatEntry fmt = ModConfig.get().getFormat(type);
        if (fmt != null && fmt.prefix != null) {
            return fmt.prefix;
        }
        return getFallbackPrefix(type);
    }

    private static String getFallbackPrefix(DamageType type) {
        return switch (type) {
            case INSTANT_KILL -> "\u26A1 INSTANT KILL";
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
