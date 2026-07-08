package com.chen1335.geneChip.client;

import com.chen1335.geneChip.client.animation.modifier.GCSpeedModifier;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranimcore.enums.PlayState;
import net.minecraft.client.player.AbstractClientPlayer;
import org.jetbrains.annotations.NotNull;

/**
 * 基因芯片专用的玩家动画控制器。
 * 基于新库的 {@link PlayerAnimationController}，持有 {@link GCSpeedModifier} 引用便于业务代码调整播放速度。
 * 只使用手动触发（triggerAnimation / replaceAnimationWithFade），因此状态处理器恒返回 {@link PlayState#STOP}。
 */
public class GCModifierLayer extends PlayerAnimationController {
    public final GCSpeedModifier speedModifier;

    public GCModifierLayer(@NotNull AbstractClientPlayer player) {
        super(player, (controller, state, setter) -> PlayState.STOP);
        this.speedModifier = new GCSpeedModifier(player, 1);
        this.addModifierBefore(this.speedModifier);
    }
}
