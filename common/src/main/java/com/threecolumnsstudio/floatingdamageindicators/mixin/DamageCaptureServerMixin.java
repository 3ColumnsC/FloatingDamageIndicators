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
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class DamageCaptureServerMixin {

    @Inject(method = "hurt", at = @At("HEAD"))
    private void fdi$recordHealth(DamageSource source, float damage, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;
        DamageCaptureState.recordHealth(self, self.level().getGameTime());
    }

    @Inject(method = "hurt", at = @At("RETURN"))
    private void fdi$onHurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (self.level().isClientSide()) return;

        if (!cir.getReturnValue()
                || !ModConfig.get().showDamage
                || FloatingDamageIndicators.DAMAGE_PACKET_SENDER == null) {
            DamageCaptureState.removeInitialHealth(self.getId());
            return;
        }

        DamageNotifier.onEntityHurt((ServerLevel) self.level(), self, source, amount);
    }
}