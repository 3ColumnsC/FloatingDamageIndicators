package com.threecolumnsstudio.floatingdamageindicators.neoforge;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.client.screen.FloatingDamageIndicatorsConfigScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public class FloatingDamageIndicatorsNeoForgeClient {

    public static void init() {
        NeoForge.EVENT_BUS.register(FloatingDamageIndicatorsNeoForgeClient.class);
        registerConfigScreen();
    }

    private static void registerConfigScreen() {
        ModList.get().getModContainerById(FloatingDamageIndicators.MOD_ID).ifPresent(container ->
            container.registerExtensionPoint(IConfigScreenFactory.class,
                (modContainer, parent) -> new FloatingDamageIndicatorsConfigScreen(parent)));
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FloatingDamageIndicators.RENDERER.tick();
    }
}
