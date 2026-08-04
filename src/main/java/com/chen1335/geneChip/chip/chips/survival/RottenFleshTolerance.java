package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class RottenFleshTolerance extends Chip {
    public RottenFleshTolerance() {
        super(makeTexture("rotten_flesh_tolerance"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
