package com.threecolumnsstudio.floatingdamageindicators.fabric;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.network.S2CDamagePacket;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;

public class FloatingDamageIndicatorsFabricClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            FloatingDamageIndicators.RENDERER.tick();
        });

        ClientPlayNetworking.registerGlobalReceiver(S2CDamagePacket.TYPE, (packet, context) -> {
            context.client().execute(packet::enqueue);
        });

        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            var mc = net.minecraft.client.Minecraft.getInstance();
            var camera = mc.gameRenderer.getMainCamera();
            FloatingDamageIndicators.RENDERER.render(
                context.matrixStack(),
                context.consumers(),
                camera.getPosition(),
                camera.rotation(),
                mc.getTimer().getGameTimeDeltaPartialTick(true)
            );
        });
    }
}
