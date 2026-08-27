package com.threecolumnsstudio.floatingdamageindicators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import com.threecolumnsstudio.floatingdamageindicators.server.ServerDamageData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DamageNumberRenderer {
    private static final int MAX_ENTRIES = 50;
    private static final double RISE_PER_TICK = 0.04;
    private static final int FADE_START_OFFSET = 16;
    private static final int FADE_END_OFFSET = 2;
    private static final float MIN_ALPHA = 0.04f;
    private static final float SCALE = 0.025f;

    private final List<DamageNumberEntry> entries = new CopyOnWriteArrayList<>();

    public void add(DamageNumberEntry entry) {
        if (entries.size() >= MAX_ENTRIES) {
            entries.removeFirst();
        }
        entries.add(entry);
    }

    public void tick() {
        ServerDamageData data;
        while ((data = ServerDamageData.QUEUE.poll()) != null) {
            ModConfig.FormatEntry fmt = ModConfig.get().getFormat(data.type());
            if (fmt != null && !fmt.enabled) continue;
            add(new DamageNumberEntry(data.position(), data.damage(), data.type()));
        }
        entries.removeIf(DamageNumberEntry::isExpired);
        for (DamageNumberEntry entry : entries) {
            entry.age++;
        }
    }

    public void render(PoseStack poseStack, MultiBufferSource consumers, Vec3 cameraPos, Quaternionf cameraRotation, float partialTick) {
        if (entries.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        if (font == null) return;

        for (DamageNumberEntry entry : entries) {
            float smoothAge = entry.age + partialTick;
            double yOffset = smoothAge * RISE_PER_TICK;
            int fadeStart = DamageNumberEntry.LIFETIME - FADE_START_OFFSET;
            int fadeEnd = DamageNumberEntry.LIFETIME - FADE_END_OFFSET;
            float alpha = smoothAge <= fadeStart ? 1.0f : 1.0f - (smoothAge - fadeStart) / (fadeEnd - fadeStart);
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));

            int alphaInt = alpha <= MIN_ALPHA ? 0 : Math.max(0, Math.min(255, (int) (alpha * 255)));
            if (alphaInt == 0) continue;

            Vec3 pos = entry.position;
            double x = pos.x - cameraPos.x;
            double y = pos.y + yOffset - cameraPos.y;
            double z = pos.z - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(cameraRotation);
            poseStack.scale(SCALE, -SCALE, SCALE);

            int argb = DamageClassifier.getColor(entry.type);
            int color = (alphaInt << 24) | (argb & 0x00FFFFFF);

            int bgColor = 0;

            Matrix4f matrix = poseStack.last().pose();
            float textWidth = font.width(entry.cachedSequence);
            font.drawInBatch(entry.cachedSequence, -textWidth / 2, 0, color, true, matrix, consumers, Font.DisplayMode.NORMAL, bgColor, 0xF000F0);

            poseStack.popPose();
        }
    }
}