package com.threecolumnsstudio.floatingdamageindicators.fabric;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.network.S2CDamagePacket;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

public class FloatingDamageIndicatorsFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        FloatingDamageIndicators.init(FabricLoader.getInstance().getConfigDir());

        PayloadTypeRegistry.playS2C().register(S2CDamagePacket.TYPE, S2CDamagePacket.CODEC);

        FloatingDamageIndicators.DAMAGE_PACKET_SENDER = (player, pos, damage, type) ->
            ServerPlayNetworking.send(player, new S2CDamagePacket(pos, damage, type));
    }
}
