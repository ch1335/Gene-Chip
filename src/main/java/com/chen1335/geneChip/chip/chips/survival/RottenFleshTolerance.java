package com.chen1335.geneChip.chip.chips.survival;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class RottenFleshTolerance extends Chip {
    public final JsValueCalculator eatExp = expValue("eat_exp", "8");
    public final JsValueCalculator recoveryChance = new JsValueCalculator("0.1", true);
    public final JsValueCalculator recoveryAmount = new JsValueCalculator("1");

    public RottenFleshTolerance() {
        super(makeTexture("rotten_flesh_tolerance"));
        registerConfigValue("recovery_chance", recoveryChance);
        registerConfigValue("recovery_amount", recoveryAmount);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(recoveryChance.getArgValue(lvl), recoveryAmount.getArgValue(lvl));
    }
}
