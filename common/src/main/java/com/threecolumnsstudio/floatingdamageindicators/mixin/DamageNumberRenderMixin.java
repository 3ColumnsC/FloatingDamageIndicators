package com.threecolumnsstudio.floatingdamageindicators.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.threecolumnsstudio.floatingdamageindicators.FloatingDamageIndicators;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.SubmitNodeStorage;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.state.level.LevelRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class DamageNumberRenderMixin {

    @Shadow @Final private LevelRenderState levelRenderState;

    @Shadow @Final private SubmitNodeStorage submitNodeStorage;

    @Inject(
        method = "render",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher;prepareFrame(Lnet/minecraft/client/renderer/SubmitNodeStorage;)Lnet/minecraft/client/renderer/feature/FeatureRenderDispatcher$PreparedFrame;"
        )
    )
    private void fdi$renderDamageNumbers(CallbackInfo ci) {
        if (this.levelRenderState == null) return;

        CameraRenderState cameraState = levelRenderState.cameraRenderState;
        if (cameraState == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        if (this.submitNodeStorage == null) return;

        PoseStack poseStack = new PoseStack();
        FloatingDamageIndicators.RENDERER.render(
                poseStack,
                cameraState.pos,
                cameraState.orientation,
                mc.getDeltaTracker().getGameTimeDeltaPartialTick(true),
                this.submitNodeStorage
        );
    }
}
