package com.chen1335.geneChip.client;

import com.chen1335.geneChip.GeneChip;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Quaternionf;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 卡牌产生额外掉落 / 战利品时，在世界对象位置生成一个物品 icon 广告牌，随时间上浮并淡出。
 * 纯客户端特效，数据由 {@link com.chen1335.geneChip.network.WorldItemFeedbackPacket} 驱动。
 */
@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class WorldItemFeedbackRenderer {
    /** 特效存活时长（毫秒）。 */
    private static final long LIFETIME_MS = 1400L;
    /** 存活期内总上浮高度（方块）。 */
    private static final float RISE = 0.6F;
    /** icon 缩放。 */
    private static final float SCALE = 0.55F;

    private record Entry(double x, double y, double z, ItemStack stack, long spawnMs) {}

    private static final List<Entry> ENTRIES = new ArrayList<>();

    /** 由网络包 handler 在客户端线程调用，新增一个物品 icon 特效。 */
    public static void add(double x, double y, double z, ItemStack stack) {
        if (stack == null || stack.isEmpty()) return;
        ENTRIES.add(new Entry(x, y, z, stack.copy(), Util.getMillis()));
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        if (ENTRIES.isEmpty()) return;

        Minecraft minecraft = Minecraft.getInstance();
        long now = Util.getMillis();
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        Quaternionf camRot = camera.rotation();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource buffer = minecraft.renderBuffers().bufferSource();

        Iterator<Entry> it = ENTRIES.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            long age = now - entry.spawnMs();
            if (age >= LIFETIME_MS) {
                it.remove();
                continue;
            }

            float t = (float) age / LIFETIME_MS;
            float rise = RISE * t;

            poseStack.pushPose();
            poseStack.translate(entry.x() - camPos.x, entry.y() + rise - camPos.y, entry.z() - camPos.z);
            poseStack.mulPose(camRot);
            poseStack.scale(SCALE, SCALE, SCALE);

            minecraft.getItemRenderer().renderStatic(
                    entry.stack(),
                    ItemDisplayContext.GROUND,
                    0x00F000F0,
                    OverlayTexture.NO_OVERLAY,
                    poseStack,
                    buffer,
                    minecraft.level,
                    0
            );

            poseStack.popPose();
        }

        buffer.endBatch();
    }
}
