package dev.unikon.enhancedvision;

import com.google.gson.JsonSyntaxException;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.logging.LogUtils;
import dev.unikon.enhancedvision.compat.VivecraftCompat;
import java.io.IOException;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.GlStateBackup;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;

/** One complete eye at a time, before Vivecraft's FSAA, overlays and runtime submission. */
public final class VrVisionRenderer {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation EFFECT = ResourceLocation.fromNamespaceAndPath(
            EnhancedVision.MOD_ID, "shaders/post/vision_vr.json");
    private static final GlStateBackup GL_STATE = new GlStateBackup();

    private static PostChain chain;
    private static RenderTarget chainTarget;
    private static int width;
    private static int height;
    private static boolean loadFailed;
    private static int activeEye = -1;
    private static float overscan = 1.0f;
    private static Matrix4f nativeProjection;

    private VrVisionRenderer() {
    }

    public static void beginEye(int eye, boolean renderLevel) {
        endEye();
        Minecraft minecraft = Minecraft.getInstance();
        if (eye < 0 || !renderLevel || !VivecraftCompat.isVrRunning()
                || minecraft.level == null || minecraft.screen != null
                || !VisionConfig.ENABLED.get() || minecraft.gameRenderer.isPanoramicMode()) {
            return;
        }
        double strength = VisionConfig.STRENGTH.get();
        if (strength == 0.0 || loadFailed || !prepareShader(minecraft)) {
            return;
        }
        overscan = (float) (1.0 + strength);
        activeEye = eye;
    }

    /** Do not mutate Vivecraft's cached matrices: they also serve non-eye render passes. */
    public static Matrix4f prepareProjection(int eye, Matrix4f projection) {
        if (activeEye < 0 || eye != activeEye) {
            return projection;
        }
        nativeProjection = new Matrix4f(projection);
        return new Matrix4f(projection)
                .m00(projection.m00() / overscan)
                .m11(projection.m11() / overscan);
    }

    /** The source view needs the pixels normally discarded by the headset's hidden-area mesh. */
    public static boolean shouldRenderFullEye() {
        return activeEye >= 0;
    }

    public static void finishEye(DeltaTracker deltaTracker) {
        if (activeEye < 0 || nativeProjection == null) {
            return;
        }
        RenderSystem.backupGlState(GL_STATE);
        boolean stencilEnabled = GL11.glIsEnabled(GL11.GL_STENCIL_TEST);
        try (RenderBindings bindings = new RenderBindings()) {
            // All world-space hands and 3D HUD geometry used the same expanded projection.
            // Apply its inverse remap to the complete eye, before the native eye postprocess.
            RenderSystem.disableBlend();
            RenderSystem.disableDepthTest();
            RenderSystem.depthMask(false);
            RenderSystem.disableCull();
            RenderSystem.disableScissor();
            GL11.glDisable(GL11.GL_STENCIL_TEST);
            RenderSystem.colorMask(true, true, true, true);
            chain.setUniform("Overscan", overscan);
            chain.setUniform("ProjectionScaleX", nativeProjection.m00());
            chain.setUniform("ProjectionScaleY", nativeProjection.m11());
            chain.setUniform("ProjectionOffsetX", nativeProjection.m20());
            chain.setUniform("ProjectionOffsetY", nativeProjection.m21());
            chain.process(deltaTracker.getGameTimeDeltaTicks());

            chainTarget.bindWrite(true);
            RenderSystem.disableBlend();
            chain.getTempTarget("vision_vr_swap").blitToScreen(width, height, false);
        } finally {
            if (stencilEnabled) {
                GL11.glEnable(GL11.GL_STENCIL_TEST);
            }
            RenderSystem.restoreGlState(GL_STATE);
        }
    }

    public static void endEye() {
        activeEye = -1;
        nativeProjection = null;
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
            LOGGER.error("Could not load Enhanced Vision VR shader. Reload resources with F3+T to retry.", exception);
            minecraft.gui.setOverlayMessage(Component.translatable("message.enhanced_vision.shader_error"), false);
            return false;
        } finally {
            RenderSystem.restoreGlState(GL_STATE);
            bindings.close();
        }
    }

    public static void reset() {
        endEye();
        if (chain != null) {
            try (RenderBindings bindings = new RenderBindings()) {
                chain.close();
            }
            chain = null;
        }
        chainTarget = null;
        loadFailed = false;
    }
}
