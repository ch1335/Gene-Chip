package com.chen1335.geneChip.chip.chips.tactics;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class SlidingTackle extends Chip {
    public final JsValueCalculator slideDistance = new JsValueCalculator("4");
    public final JsValueCalculator slideTime = new JsValueCalculator("20");
    public final JsValueCalculator damageReduction = new JsValueCalculator("0.5");
    public final JsValueCalculator cooldown = new JsValueCalculator("8");

    public SlidingTackle() {
        super(makeTexture("sliding_tackle"));
        registerConfigValue("slide_distance", slideDistance);
        registerConfigValue("slide_time", slideTime);
        registerConfigValue("damage_reduction", damageReduction);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }
}
