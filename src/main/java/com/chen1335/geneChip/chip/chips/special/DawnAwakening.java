package com.chen1335.geneChip.chip.chips.special;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

public class DawnAwakening extends Chip {
    public final JsValueCalculator healthRestore = new JsValueCalculator("2");

    public DawnAwakening() {
        super(makeTexture("dawn_awakening"));
        registerConfigValue("health_restore", healthRestore);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        long dayTime = player.level().getDayTime() % 24000;
        long prevDayTime = (player.level().getDayTime() - 1) % 24000;

        // 检测时间跨越6:00（tick 0）的时刻
        if (prevDayTime >= 23999 && dayTime < 1) {
            // 清除所有负面状态
            for (MobEffectInstance effect : player.getActiveEffects()) {
                if (!effect.getEffect().value().isBeneficial()) {
                    player.removeEffect(effect.getEffect());
                }
            }

            // 恢复生命值
            float healthAmount = healthRestore.getValue(instance.getLvl());
            player.heal(healthAmount);

            // 恢复全部饱和度
            player.getFoodData().setFoodLevel(20);
            player.getFoodData().setSaturation(20);

            // 黎明触发反馈：玩家附近暖色白色烟花粒子 + HUD icon（每个黎明仅触发一次）
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.FIREWORK,
                        player.getX(), player.getY() + 1.2, player.getZ(), 16, 0.5, 0.7, 0.5, 0.05);
                com.chen1335.geneChip.common.CardHudSyncService.feedback(serverPlayer,
                        com.chen1335.geneChip.network.CardFeedbackPacket.FeedbackType.DAWN_AWAKENING, 0);
            }
        }
    }
}
