package com.chen1335.geneChip.client.animation;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.network.AnimationPack;
import dev.kosmx.playerAnim.api.layered.IActualAnimation;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public class AnimationHandler {
    public static ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "animation");

    //播放客户端动画
    public static void playAnimation(Player player, ResourceLocation location) {
        if (location == null) {
            location = AnimationPack.EMPTY_ANIMATION;
        }

        ModifierLayer<IAnimation> animation = (ModifierLayer<IAnimation>) PlayerAnimationAccess.getPlayerAssociatedData((AbstractClientPlayer) player).get(AnimationHandler.ANIMATION_RESOURCE);
        if (animation == null) {
            return;
        }
        if (location.equals(AnimationPack.EMPTY_ANIMATION)) {
            animation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), null);
        } else {
            IActualAnimation<?> iActualAnimation = PlayerAnimationRegistry
                    .getAnimation(location)
                    .playAnimation();
            animation.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), iActualAnimation);

        }

    }

    //播放并且分发动画包来通知服务器其他玩家
    public static void playAnimationAndDistribute(Player player, ResourceLocation location) {
        if (location == null) {
            location = AnimationPack.EMPTY_ANIMATION;
        }
        playAnimation(player,location);
        PacketDistributor.sendToServer(new AnimationPack(player.getId(), location));
    }
}
