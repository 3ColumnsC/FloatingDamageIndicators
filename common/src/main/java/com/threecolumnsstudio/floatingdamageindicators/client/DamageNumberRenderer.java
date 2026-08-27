package com.threecolumnsstudio.floatingdamageindicators.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.threecolumnsstudio.floatingdamageindicators.ModConfig;
import com.threecolumnsstudio.floatingdamageindicators.server.ServerDamageData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DamageNumberRenderer {
    private static final int MAX_ENTRIES = 50;
    private static final double RISE_PER_TICK = 0.04;
    private static final int FADE_TICKS = 15;
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

    public void render(PoseStack poseStack, Vec3 cameraPos, Quaternionf cameraRotation, float partialTick, SubmitNodeStorage storage) {
        if (entries.isEmpty()) return;

        Font font = Minecraft.getInstance().font;
        if (font == null) return;

        for (DamageNumberEntry entry : entries) {
            float smoothAge = entry.age + partialTick;
            double yOffset = smoothAge * RISE_PER_TICK;
            int fadeStart = DamageNumberEntry.LIFETIME - FADE_TICKS;
            float alpha = smoothAge < fadeStart ? 1.0f : 1.0f - (smoothAge - fadeStart) / (float) FADE_TICKS;

            Vec3 pos = entry.position;
            double x = pos.x - cameraPos.x;
            double y = pos.y + yOffset - cameraPos.y;
            double z = pos.z - cameraPos.z;

            poseStack.pushPose();
            poseStack.translate(x, y, z);
            poseStack.mulPose(cameraRotation);
            poseStack.scale(SCALE, -SCALE, SCALE);

            int rgb = DamageClassifier.getColor(entry.type);
            int alphaInt = Math.max(0, Math.min(255, (int) (alpha * 255)));
            int color = (alphaInt << 24) | (rgb & 0x00FFFFFF);

            float textWidth = font.width(entry.cachedText);
            storage.submitText(poseStack, -textWidth / 2, 0, entry.cachedSequence, true, Font.DisplayMode.NORMAL, LightCoordsUtil.FULL_BRIGHT, color, 0, 0);

            poseStack.popPose();
        }
    }
}