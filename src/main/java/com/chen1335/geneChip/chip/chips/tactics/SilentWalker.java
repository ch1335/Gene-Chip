package com.chen1335.geneChip.chip.chips.tactics;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class SilentWalker extends Chip {
    public final JsValueCalculator fallExp = expValue("fall_exp", "2");

    public final JsValueCalculator maxDamageReduction = new JsValueCalculator("0.5",true);
    public final JsValueCalculator minDamageReduction = new JsValueCalculator("0.1",true);
    public final JsValueCalculator referenceDistance = new JsValueCalculator("10");

    public SilentWalker() {
        super(makeTexture("silent_walker"));
        registerConfigValue("max_damage_reduction", maxDamageReduction);
        registerConfigValue("min_damage_reduction", minDamageReduction);
        registerConfigValue("reference_distance", referenceDistance);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(maxDamageReduction.getArgValue(lvl), minDamageReduction.getArgValue(lvl), referenceDistance.getArgValue(lvl));
    }
}
