package com.chen1335.geneChip.chip.chips.tactics;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class FlyingKick extends Chip {
    public final JsValueCalculator saturationCost = new JsValueCalculator("2");
    public final JsValueCalculator damageMultiplier = new JsValueCalculator("2.5");
    public final JsValueCalculator knockbackDistance = new JsValueCalculator("8");
    public final JsValueCalculator impactDamageMultiplier = new JsValueCalculator("1.5");
    public final JsValueCalculator stunDuration = new JsValueCalculator("2");
    public final JsValueCalculator cooldown = new JsValueCalculator("8");

    public FlyingKick() {
        super(makeTexture("flying_kick"));
        registerConfigValue("saturation_cost", saturationCost);
        registerConfigValue("damage_multiplier", damageMultiplier);
        registerConfigValue("knockback_distance", knockbackDistance);
        registerConfigValue("impact_damage_multiplier", impactDamageMultiplier);
        registerConfigValue("stun_duration", stunDuration);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }
}
