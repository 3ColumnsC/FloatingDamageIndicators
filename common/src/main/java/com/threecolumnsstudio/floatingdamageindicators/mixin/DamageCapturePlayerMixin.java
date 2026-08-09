package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.util.DamageCaptureState;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class DamageCapturePlayerMixin {

    @Inject(method = "actuallyHurt", at = @At("HEAD"))
    private void fdi$recordPlayerHealth(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        LivingEntity target = (LivingEntity) (Object) this;
        float total = target.getHealth() + target.getAbsorptionAmount();
        DamageCaptureState.putInitialHealth(target.getUUID(), total, total >= target.getMaxHealth() - 0.01f, level.getGameTime());
    }

    @Inject(method = "actuallyHurt", at = @At("RETURN"))
    private void fdi$capturePlayerDamage(ServerLevel level, DamageSource source, float damage, CallbackInfo ci) {
        LivingEntity target = (LivingEntity) (Object) this;
        float initial = DamageCaptureState.removeInitialHealth(target.getUUID());
        if (!Float.isNaN(initial)) {
            float total = target.getHealth() + target.getAbsorptionAmount();
            DamageCaptureState.putActualDamage(target.getUUID(), Math.max(0, initial - total), level.getGameTime());
        }
    }
}