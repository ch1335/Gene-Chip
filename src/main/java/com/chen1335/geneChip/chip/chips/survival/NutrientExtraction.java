package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class NutrientExtraction extends Chip {
    public final JsValueCalculator foodAdd = new JsValueCalculator("2");
    public final JsValueCalculator heal = new JsValueCalculator("1");
    public NutrientExtraction() {
        super(makeTexture("nutrient_extraction"));
        registerConfigValue("food_add", foodAdd);
        registerConfigValue("heal", heal);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
