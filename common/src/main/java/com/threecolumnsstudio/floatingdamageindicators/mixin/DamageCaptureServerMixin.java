package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.threecolumnsstudio.floatingdamageindicators.DamageClassification;
import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ServerDamageTracker;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mixin(LivingEntity.class)
public class DamageCaptureServerMixin {

    private static final Map<UUID, Long> LAST_HIT_TICK = new HashMap<>();
    private static final long DEBOUNCE_TICKS = 1;

    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void fdi$onHurtServer(ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) return;

        Entity target = (Entity) (Object) this;
        Entity attacker = source.getEntity();
        long gameTime = level.getGameTime();

        if (attacker instanceof ServerPlayer attackerPlayer) {
            UUID key = target.getUUID();
            Long lastTick = LAST_HIT_TICK.get(key);
            if (lastTick != null && gameTime - lastTick <= DEBOUNCE_TICKS) {
                return;
            }
            LAST_HIT_TICK.put(key, gameTime);

            ServerDamageTracker.track(target.getUUID(), attackerPlayer.getUUID(), gameTime);

            Vec3 pos = target.position().add(0, target.getBbHeight() * 0.85, 0);
            DamageType type = DamageClassification.classifyDirect(source, attackerPlayer);

            if (FloatingDamageIndicators.DAMAGE_PACKET_SENDER != null) {
                FloatingDamageIndicators.DAMAGE_PACKET_SENDER.send(attackerPlayer, pos, amount, type);
            }
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

        if (isFire && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)) return;

        if (isPoison && !ServerDamageTracker.isRecentlyHit(targetUUID, gameTime)
                && !((LivingEntity) target).hasEffect(MobEffects.POISON)) return;

        UUID playerUuid = ServerDamageTracker.getTrackingPlayer(targetUUID, gameTime);
        if (playerUuid == null) return;

        ServerPlayer targetPlayer = level.getServer().getPlayerList().getPlayer(playerUuid);
        if (targetPlayer == null) return;

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

        if (FloatingDamageIndicators.DAMAGE_PACKET_SENDER != null) {
            FloatingDamageIndicators.DAMAGE_PACKET_SENDER.send(targetPlayer, pos, amount, dmgType);
        }
    }
}
