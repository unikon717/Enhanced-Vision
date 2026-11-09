package dev.unikon.enhancedvision.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.unikon.enhancedvision.VisionRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRendererMixin {
    @Inject(method = "render(Lnet/minecraft/client/DeltaTracker;Z)V", at = @At("HEAD"))
    private void enhancedVision$beginFrame(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci) {
        VisionRenderer.beginFrame();
    }

    // The returned FOV feeds both world projection and culling. Hand projection is a separate call site.
    @ModifyExpressionValue(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;getFov(Lnet/minecraft/client/Camera;FZ)D"))
    private double enhancedVision$worldFov(double fov) {
        return VisionRenderer.prepareWorldFov(fov);
    }

    @Inject(
            method = "renderLevel(Lnet/minecraft/client/DeltaTracker;)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/neoforged/neoforge/client/ClientHooks;dispatchRenderStage(Lnet/neoforged/neoforge/client/event/RenderLevelStageEvent$Stage;Lnet/minecraft/client/renderer/LevelRenderer;Lcom/mojang/blaze3d/vertex/PoseStack;Lorg/joml/Matrix4f;Lorg/joml/Matrix4f;ILnet/minecraft/client/Camera;Lnet/minecraft/client/renderer/culling/Frustum;)V",
                    shift = At.Shift.AFTER))
    private void enhancedVision$worldBeforeHand(DeltaTracker deltaTracker, CallbackInfo ci) {
        VisionRenderer.renderWorld(deltaTracker);
    }

    @WrapOperation(
            method = "render(Lnet/minecraft/client/DeltaTracker;Z)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/LevelRenderer;doEntityOutline()V"))
    private void enhancedVision$outline(LevelRenderer renderer, Operation<Void> original) {
        if (!VisionRenderer.outlinesComposited()) {
            original.call(renderer);
        }
    }

    @Inject(method = "close()V", at = @At("HEAD"))
    private void enhancedVision$close(CallbackInfo ci) {
        VisionRenderer.resetAll();
    }
}
