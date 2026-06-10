package com.chen1335.geneChip.chip.chips.special;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class CounterStorm extends Chip {
    public final JsValueCalculator damageReflectRatio = new JsValueCalculator("0.5",true);
    public final JsValueCalculator reflectWindow = new JsValueCalculator("3");

    public CounterStorm() {
        super(makeTexture("counter_storm"));
        registerConfigValue("damage_reflect_ratio", damageReflectRatio);
        registerConfigValue("reflect_window", reflectWindow);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }
}
