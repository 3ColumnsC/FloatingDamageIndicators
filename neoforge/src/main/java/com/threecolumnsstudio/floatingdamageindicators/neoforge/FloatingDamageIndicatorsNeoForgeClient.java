package com.threecolumnsstudio.floatingdamageindicators.neoforge;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;

public class FloatingDamageIndicatorsNeoForgeClient {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FloatingDamageIndicators.RENDERER.tick();
    }
}
