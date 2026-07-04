package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.DamageClassification;
import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import com.threecolumnsstudio.floatingdamageindicators.ServerDamageTracker;
import com.threecolumnsstudio.floatingdamageindicators.util.DamageCaptureState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class DamageCaptureServerMixin {

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void fdi$recordHealth(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        DamageCaptureState.putInitialHealth(((LivingEntity) (Object) this).getId(), ((LivingEntity) (Object) this).getHealth());
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void fdi$captureDamage(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        LivingEntity target = (LivingEntity) (Object) this;
        float initial = DamageCaptureState.removeInitialHealth(target.getId());
        if (!Float.isNaN(initial)) {
            DamageCaptureState.putActualDamage(target.getId(), Math.max(0, initial - target.getHealth()));
        }
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void fdi$onHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!ModConfig.get().showDamage) return;

        var sender = FloatingDamageIndicators.DAMAGE_PACKET_SENDER;
        if (sender == null) return;

        LivingEntity target = (LivingEntity) (Object) this;
        float stored = DamageCaptureState.removeActualDamage(target.getId());
        float actual = Float.isNaN(stored) ? amount : stored;

        Entity attacker = source.getEntity();
        long gameTime = level.getGameTime();

        if (attacker instanceof ServerPlayer attackerPlayer) {
            ServerDamageTracker.track(target.getUUID(), attackerPlayer.getUUID(), gameTime);

            Vec3 pos = target.position().add(0, target.getBbHeight() * 0.85, 0);
            DamageType type;
            ModConfig.FormatEntry killFmt = ModConfig.get().getFormat(DamageType.INSTANT_KILL);
            boolean oneShot = target.isDeadOrDying() && (target.getHealth() + actual >= target.getMaxHealth() - 0.01f);
            if (oneShot && killFmt != null && killFmt.enabled) {
                type = DamageType.INSTANT_KILL;
            } else {
                type = DamageClassification.classifyDirect(source, attackerPlayer);
            }

            sender.send(attackerPlayer, pos, actual, type);
            return;
        }

        if (target instanceof ServerPlayer targetPlayer) {
            if (!ModConfig.get().showReceivedDamage) return;

            Vec3 pos = target.position().add(0, target.getBbHeight() * 0.85, 0);

            sender.send(targetPlayer, pos, actual, DamageType.RECEIVING);
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

        ServerPlayer trackingPlayer = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (trackingPlayer == null) return;

        UUID uuid = target.getUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        double angle;
        if (isFire) {
            angle = ((msb >> 16) & 0xFFFF) / 65536.0 * Math.PI * 2;
        } else if (isWither) {
            angle = ((msb >> 16) & 0xFFFF) / 65536.0 * Math.PI * 2;
        } else {
            angle = (lsb & 0xFFFF) / 65536.0 * Math.PI * 2;
        }
        double ox = Math.cos(angle);
        double oz = Math.sin(angle);
        Vec3 pos = target.position().add(ox, target.getBbHeight() * 0.85, oz);

        sender.send(trackingPlayer, pos, actual, dmgType);
    }
}
