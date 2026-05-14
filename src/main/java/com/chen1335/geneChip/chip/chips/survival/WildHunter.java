package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class WildHunter extends Chip {
    public WildHunter() {
        super(makeTexture("wild_hunter"));
    }


    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
