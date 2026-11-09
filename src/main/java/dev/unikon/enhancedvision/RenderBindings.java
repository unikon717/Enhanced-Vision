package dev.unikon.enhancedvision;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/** Preserves the bindings changed by render-target allocation, postprocessing and screen blits. */
final class RenderBindings implements AutoCloseable {
    private final int readFramebuffer;
    private final int drawFramebuffer;
    private final int[] viewport = new int[4];
    private final int program;
    private final int vertexArray;
    private final int arrayBuffer;
    private final int activeTexture;
    private final int texture0;

    RenderBindings() {
        RenderSystem.assertOnRenderThread();
        readFramebuffer = GL11.glGetInteger(GL30.GL_READ_FRAMEBUFFER_BINDING);
        drawFramebuffer = GL11.glGetInteger(GL30.GL_DRAW_FRAMEBUFFER_BINDING);
        GL11.glGetIntegerv(GL11.GL_VIEWPORT, viewport);
        program = GL11.glGetInteger(GL20.GL_CURRENT_PROGRAM);
        vertexArray = GL11.glGetInteger(GL30.GL_VERTEX_ARRAY_BINDING);
        arrayBuffer = GL11.glGetInteger(GL15.GL_ARRAY_BUFFER_BINDING);
        activeTexture = GL11.glGetInteger(GL13.GL_ACTIVE_TEXTURE);
        // RenderTarget changes the texture binding on the currently active unit.
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        texture0 = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
    }

    @Override
    public void close() {
        GlStateManager._glBindFramebuffer(GL30.GL_READ_FRAMEBUFFER, readFramebuffer);
        GlStateManager._glBindFramebuffer(GL30.GL_DRAW_FRAMEBUFFER, drawFramebuffer);
        RenderSystem.viewport(viewport[0], viewport[1], viewport[2], viewport[3]);
        RenderSystem.activeTexture(GL13.GL_TEXTURE0);
        RenderSystem.bindTexture(texture0);
        RenderSystem.activeTexture(activeTexture);
        GlStateManager._glUseProgram(program);
        GlStateManager._glBindVertexArray(vertexArray);
        // The restored VAO may differ from BufferUploader's most recent immediate buffer.
        BufferUploader.invalidate();
        GlStateManager._glBindBuffer(GL15.GL_ARRAY_BUFFER, arrayBuffer);
    }
}
