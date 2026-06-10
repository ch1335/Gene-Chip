package com.chen1335.geneChip.chip.chips.special;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class HunterInstinct extends Chip {
    public final JsValueCalculator maxRange = new JsValueCalculator("30");
    public final JsValueCalculator markDuration = new JsValueCalculator("15");
    public final JsValueCalculator damageBoost = new JsValueCalculator("0.15");
    public final JsValueCalculator hungerCost = new JsValueCalculator("5");
    public final JsValueCalculator cooldown = new JsValueCalculator("20");

    public HunterInstinct() {
        super(makeTexture("hunter_instinct"));
        registerConfigValue("max_range", maxRange);
        registerConfigValue("mark_duration", markDuration);
        registerConfigValue("damage_boost", damageBoost);
        registerConfigValue("hunger_cost", hungerCost);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }
}
