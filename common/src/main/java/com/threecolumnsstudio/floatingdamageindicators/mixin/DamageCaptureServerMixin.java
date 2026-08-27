package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import com.threecolumnsstudio.floatingdamageindicators.server.DamageCaptureState;
import com.threecolumnsstudio.floatingdamageindicators.server.DamageNotifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class DamageCaptureServerMixin {

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void fdi$recordHealth(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        DamageCaptureState.recordHealth((LivingEntity) (Object) this, level.getGameTime());
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void fdi$captureDamage(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        DamageCaptureState.captureDamage((LivingEntity) (Object) this, level.getGameTime());
    }

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void fdi$onHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;
        if (!ModConfig.get().showDamage) return;
        if (FloatingDamageIndicators.DAMAGE_PACKET_SENDER == null) return;

        DamageNotifier.onEntityHurt(level, (LivingEntity) (Object) this, source, amount);
    }
}