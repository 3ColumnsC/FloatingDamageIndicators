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
    public static void onRenderLevelStage(RenderLevelStageEvent.AfterEntities event) {
        var mc = net.minecraft.client.Minecraft.getInstance();
        var levelRenderState = event.getLevelRenderState();
        if (levelRenderState.cameraRenderState == null) return;
        FloatingDamageIndicators.RENDERER.render(
            event.getPoseStack(),
            mc.renderBuffers().bufferSource(),
            levelRenderState.cameraRenderState.pos,
            levelRenderState.cameraRenderState.orientation,
            mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)
        );
    }
}
