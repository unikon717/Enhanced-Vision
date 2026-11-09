package dev.unikon.enhancedvision.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.unikon.enhancedvision.VrVisionRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.vivecraft.api.client.data.RenderPass;

@Pseudo
@Mixin(targets = "org.vivecraft.client_vr.render.helpers.VRPassHelper", remap = false)
public abstract class VivecraftPassMixin {
    @WrapOperation(
            method = "renderSingleView(Lorg/vivecraft/api/client/data/RenderPass;Lnet/minecraft/client/DeltaTracker$Timer;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"),
            remap = false)
    private static void enhancedVision$renderEye(GameRenderer renderer, DeltaTracker deltaTracker, boolean renderLevel,
            Operation<Void> original, RenderPass eye, DeltaTracker.Timer frameTracker, boolean frameRenderLevel) {
        int eyeIndex = eye == RenderPass.LEFT ? 0 : eye == RenderPass.RIGHT ? 1 : -1;
        VrVisionRenderer.beginEye(eyeIndex, renderLevel);
        try {
            original.call(renderer, deltaTracker, renderLevel);
            VrVisionRenderer.finishEye(deltaTracker);
        } finally {
            VrVisionRenderer.endEye();
        }
    }
}
