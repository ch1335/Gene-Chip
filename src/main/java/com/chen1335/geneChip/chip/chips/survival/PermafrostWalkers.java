package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.resources.ResourceLocation;

public class PermafrostWalkers extends Chip {
    public final JsValueCalculator coldResistance = new JsValueCalculator("25");

    public PermafrostWalkers(ResourceLocation texture) {
        super(makeTexture("permafrost_walkers"));
        registerConfigValue("cold_resistance", coldResistance);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
