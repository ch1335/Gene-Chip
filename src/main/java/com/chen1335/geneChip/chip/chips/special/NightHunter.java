package com.chen1335.geneChip.chip.chips.special;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class NightHunter extends Chip {
    public final JsValueCalculator passiveExp = expValue("passive_exp", "10");
    public final JsValueCalculator killExp = expValue("kill_exp", "4");

    public NightHunter() {
        super(makeTexture("night_hunter"));
    }

    @Override
    public boolean updateable() {
        // 效果全部硬编码（没有可配置算法），不参与升级
        return false;
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (isNight(player)) {
            // 暗夜猎手经验：夜间每分钟 +10
            ChipExperience.grantPassive(serverPlayer, this, "passive_exp");
        }
        if (player.level().getGameTime() % 20 == 0) {
            if (isNight(player)) {
                int strengthAmp = 0; // 力量 I
                int speedAmp = 0;    // 速度 I

                // 血月联动：夜间增益提升至力量 II + 速度 II
                if (WorldFactorSynergy.isBloodMoon()) {
                    strengthAmp = 1;
                    speedAmp = 1;
                }

                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 39, strengthAmp, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 239, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 39, speedAmp, false, false));
            }
        }
    }

    private boolean isNight(Player player) {
        return player.level().isNight();
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc();
    }
}
