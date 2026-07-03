package com.threecolumnsstudio.floatingdamageindicators.neoforge;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

public class FloatingDamageIndicatorsNeoForgeClient {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        FloatingDamageIndicators.RENDERER.tick();
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
        var mc = net.minecraft.client.Minecraft.getInstance();
        FloatingDamageIndicators.RENDERER.render(
            event.getPoseStack(),
            mc.renderBuffers().bufferSource(),
            event.getCamera().getPosition(),
            event.getCamera().rotation(),
            mc.getTimer().getGameTimeDeltaPartialTick(true)
        );
    }
}
