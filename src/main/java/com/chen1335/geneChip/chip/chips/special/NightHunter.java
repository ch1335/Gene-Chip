package com.chen1335.geneChip.chip.chips.special;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class NightHunter extends Chip {
    public NightHunter() {
        super(makeTexture("night_hunter"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (player.level().getGameTime() % 20 == 0) {
            if (isNight(player)) {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 39, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 239, 0, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 39, 0, false, false));
            }
        }
    }

    private boolean isNight(Player player) {
        return player.level().isNight();
    }
}
