package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class IronLung extends Chip {
    public final JsValueCalculator decreaseAirSupplyMul = new JsValueCalculator("0.5",true);

    public IronLung() {
        super(makeTexture("iron_lung"));
        registerConfigValue("decrease_air_supply_mul", decreaseAirSupplyMul);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
