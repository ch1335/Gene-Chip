package com.chen1335.geneChip.client.animation;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.animation.modifier.GCSpeedModifier;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;

/**
 * 「蜘蛛攀爬」芯片的动画部分，纯客户端。
 * <p>
 * 爬墙物理在 common 的 {@link com.chen1335.geneChip.chip.chips.tactics.SpiderClimb#handleClimb} 里双端执行，
 * 但动画只能作用于客户端本地玩家：服务端实体是 {@code ServerPlayer}，不是 {@link AbstractClientPlayer}
 * （旧代码在这里直接强转，单人世界贴墙即崩，见 2026-10-11 的 ClassCastException 报告）；
 * 专用服务端上这个类还会被 RuntimeDistCleaner 剥掉。
 * <p>
 * 因此每个入口第一件事就是 {@code isLocalPlayer()} 判断：服务端在解析到任何客户端专属类之前就已经返回，
 * 调用方也统一套一层同样的判断。
 */
public final class SpiderClimbAnimation {
    private SpiderClimbAnimation() {
    }

    /** 贴墙攀爬中：起手攀爬动画，上爬时恢复动画速度。 */
    public static void onClimbing(Player player, boolean movingUp) {
        if (!player.isLocalPlayer()) return;
        if (player instanceof AbstractClientPlayer clientPlayer) {
            IAnimation animation = PlayerAnimationAccess.getPlayerAnimationLayer(clientPlayer, AnimationHandler.ANIMATION_RESOURCE);
            if (animation != null && !animation.isActive() && !player.onGround()) {
                AnimationHandler.playAnimationAndDistribute(player, GeneChip.id("climb"));
            }
        }
        if (movingUp) {
            setSpeed(player, 1.0F);
        }
    }

    /** 离开墙面：停掉攀爬动画。 */
    public static void stopClimbing(Player player) {
        if (!player.isLocalPlayer()) return;
        setSpeed(player, 1.0F);
        AnimationHandler.playAnimationAndDistribute(player, null);
    }

    /** 下蹲悬停 / 静止下滑时调整动画播放速度。 */
    public static void setSpeed(Player player, float speed) {
        if (!player.isLocalPlayer()) return;
        GCSpeedModifier modifier = AnimationHandler.getSpeedModifier(player);
        // playerAnimator 还没给这个玩家挂上动画层时 getSpeedModifier 返回 null，跳过即可（旧代码在这里会 NPE）。
        if (modifier != null) {
            modifier.speed = speed;
        }
    }
}
