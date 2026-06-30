package com.threecolumnsstudio.floatingdamageindicators;

import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ConcurrentLinkedQueue;

public record ServerDamageData(Vec3 position, float damage, DamageType type) {

    public static final ConcurrentLinkedQueue<ServerDamageData> QUEUE = new ConcurrentLinkedQueue<>();
}
