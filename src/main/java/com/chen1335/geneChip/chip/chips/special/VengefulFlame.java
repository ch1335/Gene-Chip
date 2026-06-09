package com.chen1335.geneChip.chip.chips.special;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class VengefulFlame extends Chip {
    public final JsValueCalculator explosionRadius = new JsValueCalculator("4");
    public final JsValueCalculator burnDuration = new JsValueCalculator("5");

    public VengefulFlame() {
        super(makeTexture("vengeful_flame"));
        registerConfigValue("explosion_radius", explosionRadius);
        registerConfigValue("burn_duration", burnDuration);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }
}
