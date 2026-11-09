package dev.unikon.enhancedvision;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.unikon.enhancedvision.compat.VivecraftCompat;
import java.io.IOException;
import net.minecraft.Util;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.client.GlStateBackup;
import org.slf4j.Logger;

public final class VisionRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(
            EnhancedVision.MOD_ID, "shaders/post/vision.json");
    private static final GlStateBackup GL_STATE = new GlStateBackup();

    private static PostChain chain;
    private static RenderTarget chainTarget;
    private static int width;
    private static int height;
    private static boolean loadFailed;
    private static boolean warpThisFrame;
    private static boolean outlinesComposited;
    private static float overscan = 1.0f;
    private static Entity sprintCamera;
    private static long lastFrameNanos;
    private static double sprintBlend;
    private static boolean vrWasActive;

    private VisionRenderer() {
    }

    public static boolean isActive(Minecraft minecraft) {
        return !VivecraftCompat.isVrRunning() && minecraft.level != null && VisionConfig.ENABLED.get()
                && (!VisionConfig.FIRST_PERSON_ONLY.get() || minecraft.options.getCameraType().isFirstPerson());
    }

    public static void beginFrame() {
        boolean vrActive = VivecraftCompat.isVrRunning();
        if (vrActive != vrWasActive) {
            vrWasActive = vrActive;
            reset();
            if (!vrActive) {
                VrVisionRenderer.reset();
            }
        }
        warpThisFrame = false;
        outlinesComposited = false;
    }

    /** Only the world call site uses this; hand and gun projection never pass through it. */
    public static double prepareWorldFov(double fov) {
        Minecraft minecraft = Minecraft.getInstance();
        warpThisFrame = false;
        if (!isActive(minecraft) || minecraft.gameRenderer.isPanoramicMode()) {
            resetSprint();
            return fov;
        }

        double sprintFov = fov * (1.0 + updateSprint(minecraft) * VisionConfig.SPRINT_STRENGTH.get());
        // Screens own their background/blur pipeline. Keep the sprint transition, but
        // pair the native world projection with no lens pass until the screen closes.
        if (minecraft.screen != null) {
            return sprintFov;
        }
        double strength = VisionMath.effectiveStrength(fov, VisionConfig.STRENGTH.get(), VisionConfig.FOLLOW_FOV.get());
        if (strength == 0.0 || loadFailed || !prepareShader(minecraft)) {
            return sprintFov;
        }

        overscan = (float) (1.0 + strength);
        warpThisFrame = true;
        return VisionMath.expandedFov(sprintFov, overscan);
    }

    private static double updateSprint(Minecraft minecraft) {
        if (!VisionConfig.SPRINT_FOV.get()) {
            resetSprint();
            return 0.0;
        }
        Entity camera = minecraft.getCameraEntity();
        long now = Util.getNanos();
        if (camera != sprintCamera) {
            resetSprint();
            sprintCamera = camera;
        }
        double elapsed = lastFrameNanos == 0L ? 0.0 : (now - lastFrameNanos) * 1.0e-9;
        lastFrameNanos = now;
        if (!minecraft.isPaused()) {
            sprintBlend = VisionMath.advanceSprint(sprintBlend, camera != null && camera.isSprinting(),
                    elapsed, VisionConfig.SPRINT_TRANSITION.get());
        }
        return sprintBlend;
    }

    private static boolean prepareShader(Minecraft minecraft) {
        RenderTarget target = minecraft.getMainRenderTarget();
        if (chain != null && chainTarget == target && width == target.width && height == target.height) {
            return true;
        }
        RenderBindings bindings = new RenderBindings();
        RenderSystem.backupGlState(GL_STATE);
        try {
            if (chain != null && chainTarget != target) {
                chain.close();
                chain = null;
                chainTarget = null;
            }
            if (chain == null) {
                chain = new PostChain(minecraft.getTextureManager(), minecraft.getResourceManager(), target, EFFECT);
                chainTarget = target;
            }
            chain.resize(target.width, target.height);
            width = target.width;
            height = target.height;
            return true;
        } catch (IOException | JsonSyntaxException exception) {
            if (chain != null) {
                chain.close();
                chain = null;
            }
            chainTarget = null;
            loadFailed = true;
            LOGGER.error("Could not load Enhanced Vision shader. Reload resources with F3+T to retry.", exception);
            minecraft.gui.setOverlayMessage(Component.translatable("message.enhanced_vision.shader_error"), false);
            return false;
        } finally {
            RenderSystem.restoreGlState(GL_STATE);
            bindings.close();
        }
    }

    /** Run after all world stages, before the first-person hand and its gun model. */
    public static void renderWorld(DeltaTracker deltaTracker) {
        if (!warpThisFrame) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        RenderTarget target = minecraft.getMainRenderTarget();
        try (RenderBindings bindings = new RenderBindings()) {
            // Glowing silhouettes must follow the same projection as the world beneath them.
            target.bindWrite(true);
            minecraft.levelRenderer.doEntityOutline();
            outlinesComposited = true;
            // We replace the later outline call, including its depth-write restoration.
            // Iris leaves depth writes off; Sodium's final framebuffer blit does not reset them.
            RenderSystem.backupGlState(GL_STATE);
            try {
                RenderSystem.disableBlend();
                RenderSystem.disableDepthTest();
                RenderSystem.depthMask(false);
                RenderSystem.disableCull();
                RenderSystem.disableScissor();
                RenderSystem.colorMask(true, true, true, true);
                chain.setUniform("Overscan", overscan);
                chain.process(deltaTracker.getGameTimeDeltaTicks());

                // Copy only color: a second PostPass would clear the main world's depth buffer.
                RenderTarget warped = chain.getTempTarget("vision_swap");
                target.bindWrite(true);
                RenderSystem.disableBlend();
                warped.blitToScreen(width, height, false);
            } finally {
                RenderSystem.restoreGlState(GL_STATE);
            }
        }
    }

    public static boolean outlinesComposited() {
        return outlinesComposited;
    }

    private static void resetSprint() {
        sprintCamera = null;
        lastFrameNanos = 0L;
        sprintBlend = 0.0;
    }

    public static void reset() {
        if (chain != null) {
            try (RenderBindings bindings = new RenderBindings()) {
                chain.close();
            }
            chain = null;
        }
        chainTarget = null;
        loadFailed = false;
        warpThisFrame = false;
        outlinesComposited = false;
        resetSprint();
    }

    public static void resetAll() {
        reset();
        VrVisionRenderer.reset();
    }
}
