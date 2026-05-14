package com.chen1335.geneChip.chip.chips.mutation;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class AdrenalGlandBurst extends Chip {
    public final JsValueCalculator threshold = new JsValueCalculator("0.3", true);
    public final JsValueCalculator effectTime = new JsValueCalculator("5");
    public final JsValueCalculator cooldown = new JsValueCalculator("60");

    public AdrenalGlandBurst() {
        super(makeTexture("adrenal_gland_burst"));
        registerConfigValue("threshold", threshold);
        registerConfigValue("effect_time", effectTime);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.MUTATION;
    }
}
