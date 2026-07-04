package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.DamageClassification;
import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import com.threecolumnsstudio.floatingdamageindicators.ServerDamageTracker;
import com.threecolumnsstudio.floatingdamageindicators.util.DamageCaptureState;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class DamageCaptureServerMixin {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void fdi$recordHealth(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        DamageCaptureState.putInitialHealth(self.getId(), self.getHealth(), self.level().getGameTime());
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void fdi$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!ModConfig.get().showDamage) return;

        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        float initial = DamageCaptureState.removeInitialHealth(self.getId());
        float actual = Float.isNaN(initial) ? amount : Math.max(0, initial - self.getHealth());

        var sender = FloatingDamageIndicators.DAMAGE_PACKET_SENDER;
        if (sender == null) return;

        Entity attacker = source.getEntity();
        long gameTime = self.level().getGameTime();

        if (attacker instanceof ServerPlayer attackerPlayer) {
            ServerDamageTracker.track(self.getUUID(), attackerPlayer.getUUID(), gameTime);

            Vec3 pos = self.position().add(0, self.getBbHeight() * 0.85, 0);
            DamageType type;
            ModConfig.FormatEntry killFmt = ModConfig.get().getFormat(DamageType.INSTANT_KILL);
            boolean oneShot = self.isDeadOrDying() && (self.getHealth() + actual >= self.getMaxHealth() - 0.01f);
            if (oneShot && killFmt != null && killFmt.enabled) {
                type = DamageType.INSTANT_KILL;
            } else {
                type = DamageClassification.classifyDirect(source, attackerPlayer);
            }

            sender.send(attackerPlayer, pos, actual, type);
            return;
        }

        if (self instanceof ServerPlayer targetPlayer) {
            if (!ModConfig.get().showReceivedDamage) return;

            Vec3 pos = self.position().add(0, self.getBbHeight() * 0.85, 0);

            sender.send(targetPlayer, pos, actual, DamageType.RECEIVING);
            return;
        }

        DamageType dmgType = DamageClassification.classifyDamage(source);
        boolean isFire = dmgType == DamageType.FIRE;
        boolean isPoison = dmgType == DamageType.POISON;
        boolean isWither = dmgType == DamageType.WITHER;

        if (!isFire && !isPoison && !isWither) return;

        UUID targetUUID = self.getUUID();

        if (isFire && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)) return;

        if (isPoison && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)
                && !self.hasEffect(MobEffects.POISON)) return;

        if (isWither && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)
                && !self.hasEffect(MobEffects.WITHER)) return;

        UUID playerUuid = ServerDamageTracker.getTrackingPlayer(targetUUID, gameTime);
        if (playerUuid == null) return;

        if (self.level().getServer() == null) return;
        ServerPlayer trackingPlayer = self.level().getServer().getPlayerList().getPlayer(playerUuid);
        if (trackingPlayer == null) return;

        UUID uuid = self.getUUID();
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
        Vec3 pos = self.position().add(ox, self.getBbHeight() * 0.85, oz);

        sender.send(trackingPlayer, pos, actual, dmgType);
    }
}
