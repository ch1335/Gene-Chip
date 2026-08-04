package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class TraumaFirstAid extends Chip {
    public TraumaFirstAid() {
        super(makeTexture("trauma_first_aid"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
