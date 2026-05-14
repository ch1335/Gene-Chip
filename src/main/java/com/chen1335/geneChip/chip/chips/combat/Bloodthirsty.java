package com.chen1335.geneChip.chip.chips.combat;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class Bloodthirsty extends Chip {
    public final JsValueCalculator healChance = new JsValueCalculator("0.3",true);
    public final JsValueCalculator healAmount = new JsValueCalculator("2");

    public Bloodthirsty() {
        super(makeTexture("bloodthirsty"));
        registerConfigValue("heal_chance", healChance);
        registerConfigValue("heal_amount", healAmount);
    }

    @Override
    public ChipType getType() {
        return ChipType.COMBAT;
    }

}
