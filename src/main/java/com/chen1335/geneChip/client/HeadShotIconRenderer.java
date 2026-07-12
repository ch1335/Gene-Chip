package com.chen1335.geneChip.client;

import com.chen1335.geneChip.GeneChip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 爆头猎手触发时，在被击杀怪物处生成一个爆头 icon 广告牌，随时间上浮并淡出。
 * 纯客户端特效，数据由 {@link com.chen1335.geneChip.network.HeadShotIconPacket} 驱动。
 */
@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class HeadShotIconRenderer {
    /** icon 贴图（复用爆头猎手芯片图标）。 */
    private static final ResourceLocation ICON =
            ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "textures/chip/chip_icons/head_shot_hunter.png");

    /** 特效存活时长（毫秒）。 */
    private static final long LIFETIME_MS = 1200L;
    /** icon 边长（方块 / 米）。 */
    private static final float SIZE = 0.6F;
    /** 存活期内总上浮高度（方块）。 */
    private static final float RISE = 0.5F;

    private record Icon(double x, double y, double z, long spawnMs) {}

    private static final List<Icon> ICONS = new ArrayList<>();

    /** 由网络包 handler 在客户端线程调用，新增一个 icon 特效。 */
    public static void add(double x, double y, double z) {
        ICONS.add(new Icon(x, y, z, Util.getMillis()));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        // 云层在 vanilla 管线里排在粒子之后、天气之前，故用 AFTER_WEATHER 阶段渲染，
        // 让 icon 绘制在云之后，避免被云层遮挡。
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        if (ICONS.isEmpty()) return;

        long now = Util.getMillis();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = Minecraft.getInstance().renderBuffers().bufferSource();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityTranslucentEmissive(ICON));

        Iterator<Icon> it = ICONS.iterator();
        while (it.hasNext()) {
            Icon icon = it.next();
            long age = now - icon.spawnMs();
            if (age >= LIFETIME_MS) {
                it.remove();
                continue;
            }

            float t = (float) age / LIFETIME_MS;
            // 淡出：前 40% 保持不透明，之后线性淡出到 0
            float alpha = t < 0.4F ? 1.0F : 1.0F - (t - 0.4F) / 0.6F;
            int alphaI = Math.max(0, Math.min(255, (int) (alpha * 255)));
            float rise = RISE * t;

            poseStack.pushPose();
            poseStack.translate(icon.x() - camPos.x, icon.y() + rise - camPos.y, icon.z() - camPos.z);
            poseStack.mulPose(camRot);
            poseStack.scale(SIZE, SIZE, SIZE);

            Matrix4f matrix = poseStack.last().pose();
            renderQuad(matrix, consumer, alphaI);

            poseStack.popPose();
        }

        buffer.endBatch(RenderType.entityTranslucentEmissive(ICON));
    }

    /** 以原点为中心画一个 1x1 的四边形（已由 poseStack 缩放/朝向摄像机）。 */
    private static void renderQuad(Matrix4f matrix, VertexConsumer consumer, int alpha) {
        int light = 0x00F000F0; // 满亮，不受世界光照影响
        float h = 0.5F;
        // 顺序保证正面朝向摄像机（billboard 已对齐摄像机旋转）
        vertex(matrix, consumer, -h, -h, 0, 1, alpha, light);
        vertex(matrix, consumer, h, -h, 1, 1, alpha, light);
        vertex(matrix, consumer, h, h, 1, 0, alpha, light);
        vertex(matrix, consumer, -h, h, 0, 0, alpha, light);
    }

    private static void vertex(Matrix4f matrix, VertexConsumer consumer,
                               float x, float y, float u, float v, int alpha, int light) {
        consumer.addVertex(matrix, x, y, 0)
                .setColor(255, 255, 255, alpha)
                .setUv(u, v)
                .setOverlay(net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0, 0, 1);
    }
}
