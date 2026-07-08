package com.chen1335.geneChip.mixinHooks.minecraft;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

@SuppressWarnings("all")
public class ItemInHandLayerHooks {
    public static void onRenderItem(LivingEntity livingEntity, ItemStack itemStack, ItemDisplayContext displayContext, HumanoidArm arm, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (!(livingEntity instanceof Player player)) return;

        AnimationApplier emote = ((IAnimatedPlayer) player).playerAnimator_getAnimation();
        if (!emote.isActive()) return;

        // 根据手臂选择对应的物品骨骼
        String partName = arm == HumanoidArm.RIGHT ? "rightItem" : "leftItem";
//
//         位置：传入 ZERO 取得动画提供的偏移量（模型像素单位，需 /16 转为方块单位）
        Vec3f pos = emote.get3DTransform(partName, TransformType.POSITION, Vec3f.ZERO);
        poseStack.translate(pos.getY() / 16.0F, pos.getX() / 16.0F, pos.getZ() / 16.0F);

        // 旋转：传入 ZERO 取得动画提供的旋转量（弧度），按 ZYX 顺序组合到 PoseStack
        Vec3f rot = emote.get3DTransform(partName, TransformType.ROTATION, Vec3f.ZERO);
        poseStack.mulPose(new Quaternionf().rotationZYX(rot.getZ(), rot.getX(), rot.getY()));
    }
}
