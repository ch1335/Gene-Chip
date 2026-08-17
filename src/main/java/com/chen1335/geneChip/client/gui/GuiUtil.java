package com.chen1335.geneChip.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Matrix4f;

import java.math.BigDecimal;

public class GuiUtil {
    public static String format(float value, int i) {
        return new BigDecimal(String.format("%." + i + "f", value)).stripTrailingZeros().toPlainString();
    }

    public static void drawTextureWithSize(ResourceLocation resourceLocation, GuiGraphics guiGraphics, float x, float y, float width, float height, int blitOffset) {
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f pose = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(pose, x, y, (float) blitOffset).setUv(0, 0);
        bufferbuilder.addVertex(pose, x, y + height, (float) blitOffset).setUv(0, 1);
        bufferbuilder.addVertex(pose, x + width, y + height, (float) blitOffset).setUv(1, 1);
        bufferbuilder.addVertex(pose, x + width, y, (float) blitOffset).setUv(1, 0);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawTextureWithSize(
            ResourceLocation resourceLocation,
            GuiGraphics guiGraphics,
            float x,
            float y,
            float width,
            float height,
            float uOffset,
            float vOffset,
            int uWidth,
            int vHeight,
            int textureWidth,
            int textureHeight,
            int blitOffset
    ) {
        float u1 = uOffset / textureWidth;
        float v1 = vOffset / textureHeight;
        float u2 = (uOffset + uWidth) / textureWidth;
        float v2 = (vOffset + vHeight) / textureHeight;
        RenderSystem.setShaderTexture(0, resourceLocation);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        Matrix4f pose = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(pose, x, y, (float) blitOffset).setUv(u1, v1);
        bufferbuilder.addVertex(pose, x, y + height, (float) blitOffset).setUv(u1, v2);
        bufferbuilder.addVertex(pose, x + width, y + height, (float) blitOffset).setUv(u2, v2);
        bufferbuilder.addVertex(pose, x + width, y, (float) blitOffset).setUv(u2, v1);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static void drawColorWithSize(
            GuiGraphics guiGraphics,
            float x,
            float y,
            float width,
            float height,
            int color,
            int blitOffset
    ) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f pose = guiGraphics.pose().last().pose();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        bufferbuilder.addVertex(pose, x, y, (float) blitOffset).setUv(0, 0).setColor(color);
        bufferbuilder.addVertex(pose, x, y + height, (float) blitOffset).setUv(0, 1).setColor(color);
        bufferbuilder.addVertex(pose, x + width, y + height, (float) blitOffset).setUv(1, 1).setColor(color);
        bufferbuilder.addVertex(pose, x + width, y, (float) blitOffset).setUv(1, 0).setColor(color);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }
}
