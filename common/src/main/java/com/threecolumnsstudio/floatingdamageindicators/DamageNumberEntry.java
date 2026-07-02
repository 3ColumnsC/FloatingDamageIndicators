package com.threecolumnsstudio.floatingdamageindicators;

import net.minecraft.world.phys.Vec3;

public class DamageNumberEntry {
    public static final int LIFETIME = 40;

    public final Vec3 position;
    public final float damage;
    public final DamageType type;
    public int age;
    public final String cachedText;

    public DamageNumberEntry(Vec3 position, float damage, DamageType type) {
        this.position = position;
        this.damage = damage;
        this.type = type;
        this.age = 0;
        this.cachedText = DamageClassifier.getPrefix(type) + String.format("%.1f", damage);
    }

    public boolean isExpired() {
        return age >= LIFETIME;
    }
}
