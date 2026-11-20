package com.chen1335.geneChip.chip.chips.special;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class IronHeart extends Chip {
    public final JsValueCalculator debuffExp = expValue("debuff_exp", "3");

    public final JsValueCalculator durationReduction = new JsValueCalculator("0.5",true);

    public IronHeart() {
        super(makeTexture("iron_heart"));
        registerConfigValue("duration_reduction", durationReduction);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(durationReduction.getArgValue(lvl));
    }
}
