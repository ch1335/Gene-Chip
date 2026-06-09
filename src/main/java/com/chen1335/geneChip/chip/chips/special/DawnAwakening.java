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
            player.getFoodData().setSaturation(20);
        }
    }
}
