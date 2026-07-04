package com.threecolumnsstudio.floatingdamageindicators;

import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;

public class DamageNumberEntry {
    public static final int LIFETIME = 40;

    public final Vec3 position;
    public final float damage;
    public final DamageType type;
    public int age;
    public final String cachedText;
    public final FormattedCharSequence cachedSequence;

    public DamageNumberEntry(Vec3 position, float damage, DamageType type) {
        if (!Float.isFinite(damage)) {
            damage = 0;
        }
        this.position = position;
        this.damage = damage;
        this.type = type;
        this.age = 0;
        String prefix = DamageClassifier.getPrefix(type);
        boolean showNum = true;
        ModConfig.FormatEntry fmt = ModConfig.get().getFormat(type);
        if (fmt != null) {
            showNum = fmt.showDamage;
        }
        String num = showNum ? String.format(Locale.ROOT, "%.1f", damage) : "";
        String sep = (!prefix.isEmpty() && !num.isEmpty() && !prefix.endsWith(" ")) ? " " : "";
        String text = prefix + sep + num;
        this.cachedText = text;
        this.cachedSequence = FormattedCharSequence.forward(text, Style.EMPTY);
    }

    public boolean isExpired() {
        return age >= LIFETIME;
    }
}
