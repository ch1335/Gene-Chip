package com.chen1335.geneChip.chip.chips.tactics;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class FlyingKick extends Chip {
    public final JsValueCalculator hitExp = expValue("hit_exp", "10");
    public final JsValueCalculator impactExp = expValue("impact_exp", "5");
    public final JsValueCalculator stunExp = expValue("stun_exp", "5");

    public final JsValueCalculator foodCost = new JsValueCalculator("2");
    public final JsValueCalculator damageMultiplier = new JsValueCalculator("2.5");
    public final JsValueCalculator knockbackDistance = new JsValueCalculator("8");
    public final JsValueCalculator impactDamageMultiplier = new JsValueCalculator("1.5");
    public final JsValueCalculator stunDuration = new JsValueCalculator("2");
    public final JsValueCalculator cooldown = new JsValueCalculator("8");

    public FlyingKick() {
        super(makeTexture("flying_kick"));
        registerConfigValue("food_cost", foodCost);
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

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(foodCost.getArgValue(lvl), damageMultiplier.getArgValue(lvl), knockbackDistance.getArgValue(lvl), impactDamageMultiplier.getArgValue(lvl), stunDuration.getArgValue(lvl), cooldown.getArgValue(lvl));
    }
}
