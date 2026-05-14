package com.chen1335.geneChip.chip.chips.combat;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class QuickAdjustment extends Chip {
    public final JsValueCalculator effectTime = new JsValueCalculator("3");

    public QuickAdjustment() {
        super(makeTexture("quick_adjustment"));
        registerConfigValue("effect_time", effectTime);
    }

    @Override
    public ChipType getType() {
        return ChipType.COMBAT;
    }
}
