package com.threecolumnsstudio.floatingdamageindicators;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;

public final class FloatingDamageIndicators {
    public static final String MOD_ID = "floatingdamageindicators";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static final DamageNumberRenderer RENDERER = new DamageNumberRenderer();

    @FunctionalInterface
    public interface DamagePacketSender {
        void send(ServerPlayer player, Vec3 pos, float damage, DamageType type);
    }

    public static volatile DamagePacketSender DAMAGE_PACKET_SENDER;

    private FloatingDamageIndicators() {}

    public static void init(Path configDir) {
        ModConfig.load(configDir);
        LOGGER.info("{} initialized", MOD_ID);
    }
}
