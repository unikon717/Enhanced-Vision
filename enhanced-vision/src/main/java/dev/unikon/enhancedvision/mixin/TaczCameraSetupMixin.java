package dev.unikon.enhancedvision.mixin;

import dev.unikon.enhancedvision.VisionConfig;
import dev.unikon.enhancedvision.VisionRenderer;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ComputeFovModifierEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/** Lets Enhanced Vision replace TACZ's fixed sprint multiplier without changing its speed or ADS settings. */
@Pseudo
@Mixin(targets = "com.tacz.guns.client.event.CameraSetupEvent", remap = false)
public abstract class TaczCameraSetupMixin {
    @ModifyConstant(
            method = "onComputeMovementFov(Lnet/neoforged/neoforge/client/event/ComputeFovModifierEvent;)V",
            constant = @Constant(floatValue = 1.15f), remap = false)
    private static float enhancedVision$replaceFixedSprint(float multiplier, ComputeFovModifierEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (VisionRenderer.isActive(minecraft) && VisionConfig.SPRINT_FOV.get()
                && event.getPlayer() == minecraft.getCameraEntity()) {
            return 1.0f;
        }
        return multiplier;
    }
}
