package com.threecolumnsstudio.floatingdamageindicators.client.screen;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;

import java.util.Locale;

import net.minecraft.network.chat.Component;

final class FormatLabels {
    private FormatLabels() {}

    static Component forType(DamageType type) {
        return Component.translatable("floatingdamageindicators.config.type." + type.name().toLowerCase(Locale.ROOT));
    }
}
