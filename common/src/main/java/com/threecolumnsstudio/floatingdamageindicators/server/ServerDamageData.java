package com.threecolumnsstudio.floatingdamageindicators.server;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import net.minecraft.world.phys.Vec3;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public record ServerDamageData(Vec3 position, float damage, DamageType type) {

    public static final int MAX_QUEUE_SIZE = 2048;

    public static final Queue<ServerDamageData> QUEUE = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
}