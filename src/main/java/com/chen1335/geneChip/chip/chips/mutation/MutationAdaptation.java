package com.chen1335.geneChip.chip.chips.mutation;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class MutationAdaptation extends Chip {
    public MutationAdaptation() {
        super(makeTexture("mutation_adaptation"));
    }

    @Override
    public ChipType getType() {
        return ChipType.MUTATION;
    }
}
