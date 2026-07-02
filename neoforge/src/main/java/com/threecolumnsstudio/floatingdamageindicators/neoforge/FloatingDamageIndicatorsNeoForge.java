package com.threecolumnsstudio.floatingdamageindicators.neoforge;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.network.S2CDamagePacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@Mod("floatingdamageindicators")
public class FloatingDamageIndicatorsNeoForge {

    public FloatingDamageIndicatorsNeoForge(IEventBus modEventBus) {
        FloatingDamageIndicators.init(FMLPaths.CONFIGDIR.get());
        modEventBus.addListener(RegisterPayloadHandlersEvent.class, this::onRegisterPayloadHandlers);
        if (isClientEnvironment()) {
            NeoForge.EVENT_BUS.register(FloatingDamageIndicatorsNeoForgeClient.class);
        }
    }

    private void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1");
        registrar.playToClient(S2CDamagePacket.TYPE, S2CDamagePacket.CODEC,
            (packet, context) -> context.enqueueWork(packet::enqueue));

        FloatingDamageIndicators.DAMAGE_PACKET_SENDER = (player, pos, damage, type) ->
            PacketDistributor.sendToPlayer(player, new S2CDamagePacket(pos, damage, type));
    }

    private static boolean isClientEnvironment() {
        try {
            Class.forName("net.neoforged.neoforge.client.event.ClientTickEvent");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
