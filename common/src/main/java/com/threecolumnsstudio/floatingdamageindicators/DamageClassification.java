package com.threecolumnsstudio.floatingdamageindicators;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public final class DamageClassification {
    private DamageClassification() {}

    public static DamageType classifyDirect(DamageSource source, Player attacker) {
        String msgId = source.getMsgId();
        if (source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK)
                || msgId.equals("player") || msgId.equals("mob")) {
            if (isCritical(attacker)) return DamageType.CRITICAL;
            return DamageType.NORMAL;
        }
        return classifyDamage(source);
    }

    public static DamageType classifyDamage(DamageSource source) {
        String msgId = source.getMsgId();
        if (source.is(DamageTypes.ARROW) || source.is(DamageTypes.TRIDENT)
                || msgId.equals("arrow") || msgId.equals("trident"))
            return DamageType.PROJECTILE;
        if (source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.IN_FIRE)
                || source.is(DamageTypes.LAVA) || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.UNATTRIBUTED_FIREBALL)
                || msgId.equals("onFire") || msgId.equals("inFire")
                || msgId.equals("lava") || msgId.equals("fireball"))
            return DamageType.FIRE;
        if (source.is(DamageTypes.WITHER) || msgId.equals("wither"))
            return DamageType.WITHER;
        if (source.is(DamageTypes.MAGIC) || source.is(DamageTypes.INDIRECT_MAGIC)
                || msgId.equals("magic") || msgId.equals("indirectMagic"))
            return DamageType.POISON;
        return DamageType.NORMAL;
    }

    public static boolean isCritical(Player attacker) {
        return attacker.fallDistance > 0.0F && !attacker.onGround()
                && !attacker.onClimbable() && !attacker.isInWater()
                && !attacker.isPassenger() && !attacker.hasEffect(MobEffects.BLINDNESS);
    }
}
