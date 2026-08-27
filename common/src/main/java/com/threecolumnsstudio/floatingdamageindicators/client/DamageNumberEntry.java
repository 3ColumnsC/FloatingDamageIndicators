package com.threecolumnsstudio.floatingdamageindicators.client;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class DamageNumberEntry {
    public static final int LIFETIME = 40;

    public final Vec3 position;
    public final float damage;
    public final DamageType type;
    public int age;
    public final String cachedText;

    public DamageNumberEntry(Vec3 position, float damage, DamageType type) {
        this.position = position;
        this.damage = Float.isFinite(damage) ? damage : 0;
        this.type = type;
        this.age = 0;
        String prefix = DamageClassifier.getPrefix(type);
        ModConfig.FormatEntry fmt = ModConfig.get().getFormat(type);
        boolean showNum = fmt == null || fmt.showDamage;
        String num = showNum ? String.format(Locale.ROOT, "%.1f", this.damage) : "";
        String sep = (!prefix.isEmpty() && !num.isEmpty() && !prefix.endsWith(" ")) ? " " : "";
        this.cachedText = prefix + sep + num;
    }

    public boolean isExpired() {
        return age >= LIFETIME;
    }
}