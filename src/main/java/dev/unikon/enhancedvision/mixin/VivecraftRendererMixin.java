package dev.unikon.enhancedvision.mixin;

import dev.unikon.enhancedvision.VrVisionRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.vivecraft.client_vr.provider.VRRenderer", remap = false)
public abstract class VivecraftRendererMixin {
    @Inject(method = "getCachedProjectionMatrix(IFF)Lorg/joml/Matrix4f;", at = @At("RETURN"), cancellable = true, remap = false)
    private void enhancedVision$eyeProjection(int eye, float nearClip, float farClip,
            CallbackInfoReturnable<Matrix4f> cir) {
        cir.setReturnValue(VrVisionRenderer.prepareProjection(eye, cir.getReturnValue()));
    }

    @Inject(method = "drawMask()V", at = @At("HEAD"), cancellable = true, remap = false)
    private void enhancedVision$fullEyeSource(CallbackInfo ci) {
        if (VrVisionRenderer.shouldRenderFullEye()) {
            ci.cancel();
        }
    }
}
