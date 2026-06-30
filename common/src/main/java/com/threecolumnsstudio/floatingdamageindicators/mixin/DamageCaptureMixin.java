package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.DamageClassification;
import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.ServerDamageData;
import com.threecolumnsstudio.floatingdamageindicators.ServerDamageTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(LivingEntity.class)
public class DamageCaptureMixin {

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void fdi$onHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity target = (Entity) (Object) this;
        Entity attacker = source.getEntity();
        long gameTime = level.getGameTime();

        if (attacker instanceof Player attackerPlayer) {
            if (!cir.getReturnValue()) return;

            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) return;
            if (!attackerPlayer.getUUID().equals(mc.player.getUUID())) return;

            ServerDamageTracker.track(target.getUUID(), mc.player.getUUID(), gameTime);

            Vec3 pos = target.position().add(0, target.getBbHeight() * 0.85, 0);
            DamageType type = DamageClassification.classifyDirect(source, attackerPlayer);
            queueDamage(pos, amount, type);
            return;
        }

        String msgId = source.getMsgId();
        boolean isFire = source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.LAVA) || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.UNATTRIBUTED_FIREBALL)
                || msgId.equals("onFire") || msgId.equals("inFire")
                || msgId.equals("lava") || msgId.equals("fireball");
        boolean isPoison = source.is(DamageTypes.MAGIC) || source.is(DamageTypes.WITHER)
                || source.is(DamageTypes.INDIRECT_MAGIC)
                || msgId.equals("magic") || msgId.equals("wither") || msgId.equals("indirectMagic");

        if (!isFire && !isPoison) return;

        UUID targetUUID = target.getUUID();

        if (isFire) {
            if (!cir.getReturnValue()) return;
            if (!ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)) return;
        }

        if (isPoison) {
            if (!ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)
                    && !((LivingEntity) target).hasEffect(MobEffects.POISON)) return;
        }

        DamageType dmgType = isFire ? DamageType.FIRE : DamageType.POISON;
        UUID uuid = target.getUUID();
        long msb = uuid.getMostSignificantBits();
        long lsb = uuid.getLeastSignificantBits();
        double angle;
        if (isFire) {
            angle = ((msb >> 16) & 0xFFFF) / 65536.0 * Math.PI * 2;
        } else {
            angle = (lsb & 0xFFFF) / 65536.0 * Math.PI * 2;
        }
        double ox = Math.cos(angle);
        double oz = Math.sin(angle);
        Vec3 pos = target.position().add(ox, target.getBbHeight() * 0.85, oz);
        queueDamage(pos, amount, dmgType);
    }

    private static void queueDamage(Vec3 pos, float amount, DamageType type) {
        ServerDamageData.QUEUE.offer(new ServerDamageData(pos, amount, type));
    }
}
