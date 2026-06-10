package com.chen1335.geneChip.chip.chips.special;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class LocksmithIntuition extends Chip {
    public final JsValueCalculator difficultyReduction = new JsValueCalculator("0.6",true);
    public final JsValueCalculator qualityThreshold = new JsValueCalculator("1.8");

    public LocksmithIntuition() {
        super(makeTexture("locksmith_intuition"));
        registerConfigValue("difficulty_reduction", difficultyReduction);
        registerConfigValue("quality_threshold", qualityThreshold);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }
}
