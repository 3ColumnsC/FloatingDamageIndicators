package com.threecolumnsstudio.floatingdamageindicators.server;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

public final class DamageNotifier {

    private DamageNotifier() {}

    public static void onEntityHurt(ServerLevel level, LivingEntity target, DamageSource source, float amount) {
        float stored = DamageCaptureState.removeActualDamage(target.getUUID());
        float actual = Float.isNaN(stored) ? amount : stored;
        boolean wasFullHealth = DamageCaptureState.consumeWasFullHealth(target.getUUID());

        Entity attacker = source.getEntity();
        long gameTime = level.getGameTime();

        if (attacker instanceof ServerPlayer attackerPlayer) {
            ServerDamageTracker.track(target.getUUID(), attackerPlayer.getUUID(), gameTime);

            Vec3 pos = target.position().add(0, target.getBbHeight() * 0.85, 0);
            DamageType type;
            ModConfig.FormatEntry killFmt = ModConfig.get().getFormat(DamageType.INSTANT_KILL);
            boolean oneShot = target.isDeadOrDying() && wasFullHealth;
            if (oneShot && killFmt != null && killFmt.enabled) {
                type = DamageType.INSTANT_KILL;
            } else {
                type = DamageClassification.classifyDirect(source, attackerPlayer);
            }

            FloatingDamageIndicators.DAMAGE_PACKET_SENDER.send(attackerPlayer, pos, actual, type);
            return;
        }

        if (target instanceof ServerPlayer targetPlayer) {
            if (!ModConfig.get().showReceivedDamage) return;

            Vec3 pos = target.position().add(0, target.getBbHeight() * 0.85, 0);

            FloatingDamageIndicators.DAMAGE_PACKET_SENDER.send(targetPlayer, pos, actual, DamageType.RECEIVING);
            return;
        }

        DamageType dmgType = DamageClassification.classifyDamage(source);
        boolean isFire = dmgType == DamageType.FIRE;
        boolean isPoison = dmgType == DamageType.POISON;
        boolean isWither = dmgType == DamageType.WITHER;

        if (!isFire && !isPoison && !isWither) return;

        UUID targetUUID = target.getUUID();

        if (isFire && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)) return;

        if (isPoison && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)
                && !target.hasEffect(MobEffects.POISON)) return;

        if (isWither && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)
                && !target.hasEffect(MobEffects.WITHER)) return;

        UUID playerUuid = ServerDamageTracker.getTrackingPlayer(targetUUID, gameTime);
        if (playerUuid == null) return;

        if (level.getServer() == null) return;
        ServerPlayer trackingPlayer = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (trackingPlayer == null) return;

        FloatingDamageIndicators.DAMAGE_PACKET_SENDER.send(trackingPlayer, effectPosition(target, dmgType), actual, dmgType);
    }

    private static Vec3 effectPosition(LivingEntity target, DamageType type) {
        UUID uuid = target.getUUID();
        long seed;
        if (type == DamageType.POISON) {
            seed = uuid.getLeastSignificantBits() & 0xFFFF;
        } else {
            seed = (uuid.getMostSignificantBits() >> 16) & 0xFFFF;
        }
        double angle = seed / 65536.0 * Math.PI * 2;
        double ox = Math.cos(angle);
        double oz = Math.sin(angle);
        return target.position().add(ox, target.getBbHeight() * 0.85, oz);
    }
}