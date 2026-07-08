package com.chen1335.geneChip.client.animation;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.GCModifierLayer;
import com.chen1335.geneChip.client.animation.modifier.GCSpeedModifier;
import com.chen1335.geneChip.network.AnimationPack;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.easing.EasingType;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class AnimationHandler {
    public static ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "animation");

    //播放客户端动画
    public static void playAnimation(Player player, ResourceLocation location) {
        if (!player.isLocalPlayer()) {
            return;
        }
        if (location == null) {
            location = AnimationPack.EMPTY_ANIMATION;
        }

        GCModifierLayer controller = getController(player);
        if (controller == null) {
            return;
        }

        if (location.equals(AnimationPack.EMPTY_ANIMATION)) {
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(2, EasingType.EASE_IN_OUT_SINE), (RawAnimation) null);
        } else {
            controller.replaceAnimationWithFade(
                    AbstractFadeModifier.standardFadeIn(2, EasingType.EASE_IN_OUT_SINE), location);
        }
    }

    //播放并且分发动画包来通知服务器其他玩家
    public static void playAnimationAndDistribute(Player player, ResourceLocation location) {
        if (!player.isLocalPlayer()) {
            return;
        }
        if (location == null) {
            location = AnimationPack.EMPTY_ANIMATION;
        }
        playAnimation(player, location);
        PacketDistributor.sendToServer(new AnimationPack(player.getId(), location));
    }

    public static GCSpeedModifier getSpeedModifier(Player player) {
        GCModifierLayer controller = getController(player);
        return controller == null ? null : controller.speedModifier;
    }

    private static GCModifierLayer getController(Player player) {
        IAnimation animation = PlayerAnimationAccess.getPlayerAnimationLayer(
                (AbstractClientPlayer) player, AnimationHandler.ANIMATION_RESOURCE);
        return animation instanceof GCModifierLayer controller ? controller : null;
    }
}
