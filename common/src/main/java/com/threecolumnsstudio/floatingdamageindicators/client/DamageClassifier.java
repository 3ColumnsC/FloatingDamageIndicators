package com.threecolumnsstudio.floatingdamageindicators.client;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
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
        return 0xFF000000 | type.defaultColor();
    }

    public static String getPrefix(DamageType type) {
        Objects.requireNonNull(type);
        FormatEntry fmt = ModConfig.get().getFormat(type);
        if (fmt != null && fmt.prefix != null) {
            return fmt.prefix;
        }
        return type.defaultPrefix();
    }
}