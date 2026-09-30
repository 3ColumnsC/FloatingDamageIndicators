package com.threecolumnsstudio.floatingdamageindicators.fabric.screen;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.threecolumnsstudio.floatingdamageindicators.client.screen.FloatingDamageIndicatorsConfigScreen;

public final class FloatingDamageIndicatorsModMenu implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return FloatingDamageIndicatorsConfigScreen::new;
    }
}
