package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class PermafrostWalkers extends Chip {
    public final JsValueCalculator coldResistance = new JsValueCalculator("25");

    public PermafrostWalkers(ResourceLocation texture) {
        super(makeTexture("permafrost_walkers"));
        registerConfigValue("cold_resistance", coldResistance);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        // 寒潮联动：额外获得抗性I
        if (WorldFactorSynergy.isColdWave()) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 39, 0, false, false));
        }
    }
}
