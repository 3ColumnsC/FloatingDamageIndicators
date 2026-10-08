package com.threecolumnsstudio.floatingdamageindicators.neoforge;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.client.screen.FloatingDamageIndicatorsConfigScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
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
