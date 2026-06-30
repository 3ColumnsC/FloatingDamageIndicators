package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.ServerDamageTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class EffectTrackingMixin {

    @Inject(method = "addEffect(Lnet/minecraft/world/effect/MobEffectInstance;Lnet/minecraft/world/entity/Entity;)Z", at = @At("HEAD"))
    private void fdi$onAddEffect(MobEffectInstance effect, Entity effectSource, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity target = (LivingEntity) (Object) this;
        if (target.level().isClientSide()) return;

        Player player = resolvePlayerSource(effectSource);
        if (player == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !player.getUUID().equals(mc.player.getUUID())) return;

        ServerDamageTracker.track(target.getUUID(), mc.player.getUUID(), target.level().getGameTime());
    }

    private static Player resolvePlayerSource(Entity source) {
        if (source instanceof Player player) return player;
        if (source instanceof Projectile projectile) {
            Entity owner = projectile.getOwner();
            if (owner instanceof Player player) return player;
        }
        return null;
    }
}
