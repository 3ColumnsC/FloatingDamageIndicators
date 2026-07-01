package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class DamageNumberRenderMixin {

    @Inject(method = "submitEntities", at = @At("RETURN"))
    private void fdi$renderDamageNumbers(PoseStack poseStack, LevelRenderState levelRenderState,
                                         SubmitNodeCollector output, CallbackInfo ci) {
        if (levelRenderState == null) return;

        CameraRenderState cameraState = levelRenderState.cameraRenderState;
        if (cameraState == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        FloatingDamageIndicators.RENDERER.render(
                poseStack,
                output,
                cameraState.pos,
                cameraState.orientation,
                mc.getDeltaTracker().getGameTimeDeltaPartialTick(true)
        );
    }
}