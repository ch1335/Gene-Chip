package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class SewagePurificationPack extends Chip {

    public SewagePurificationPack() {
        super(makeTexture("sewage_purification_pack"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
