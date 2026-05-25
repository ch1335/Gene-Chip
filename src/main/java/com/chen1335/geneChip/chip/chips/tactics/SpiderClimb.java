package com.chen1335.geneChip.chip.chips.tactics;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class SpiderClimb extends Chip {
    public final JsValueCalculator climbSpeed = new JsValueCalculator("0.3");
    public final JsValueCalculator fallDamageReduction = new JsValueCalculator("0.8");

    public SpiderClimb() {
        super(makeTexture("spider_climb"));
        registerConfigValue("climb_speed", climbSpeed);
        registerConfigValue("fall_damage_reduction", fallDamageReduction);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }
}
