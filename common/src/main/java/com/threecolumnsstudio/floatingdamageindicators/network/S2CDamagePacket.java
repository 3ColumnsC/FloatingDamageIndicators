package com.threecolumnsstudio.floatingdamageindicators.network;

import com.threecolumnsstudio.floatingdamageindicators.DamageType;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import com.threecolumnsstudio.floatingdamageindicators.ServerDamageData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

public record S2CDamagePacket(Vec3 position, float damage, DamageType damageType) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<S2CDamagePacket> TYPE = new CustomPacketPayload.Type<>(
        Identifier.fromNamespaceAndPath(FloatingDamageIndicators.MOD_ID, "damage")
    );

    public static final StreamCodec<FriendlyByteBuf, S2CDamagePacket> CODEC = StreamCodec.of(
        (buf, packet) -> packet.write(buf),
        S2CDamagePacket::read
    );

    public void write(FriendlyByteBuf buf) {
        buf.writeDouble(position.x);
        buf.writeDouble(position.y);
        buf.writeDouble(position.z);
        buf.writeFloat(damage);
        buf.writeEnum(damageType);
    }

    public static S2CDamagePacket read(FriendlyByteBuf buf) {
        Vec3 pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        float damage = buf.readFloat();
        DamageType dmgType = buf.readEnum(DamageType.class);
        return new S2CDamagePacket(pos, damage, dmgType);
    }

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void enqueue() {
        ServerDamageData.QUEUE.offer(new ServerDamageData(position, damage, damageType));
    }
}
