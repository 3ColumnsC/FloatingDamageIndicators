package com.threecolumnsstudio.floatingdamageindicators.client;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig.FormatEntry;

import java.util.Objects;

public final class DamageClassifier {
    private DamageClassifier() {}

    public static int getColor(DamageType type) {
        Objects.requireNonNull(type);
        FormatEntry format = ModConfig.get().getEffectiveFormat(type);
        return 0xFF000000 | ModConfig.parseColorRgb(format.color, type.defaultColor());
    }

    public static String getPrefix(DamageType type) {
        Objects.requireNonNull(type);
        FormatEntry format = ModConfig.get().getEffectiveFormat(type);
        return ModConfig.sanitizePrefix(type, format.prefix);
    }
}
