package com.chen1335.geneChip.chip.chips.tactics;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class TacticalRoll extends Chip {
    public final JsValueCalculator rollDistance = new JsValueCalculator("3");
    public final JsValueCalculator invincibleTime = new JsValueCalculator("0.5");
    public final JsValueCalculator offBalanceTime = new JsValueCalculator("1");
    public final JsValueCalculator offBalanceSlow = new JsValueCalculator("0.7",true);
    public final JsValueCalculator cooldown = new JsValueCalculator("8");

    public TacticalRoll() {
        super(makeTexture("tactical_roll"));
        registerConfigValue("roll_distance", rollDistance);
        registerConfigValue("invincible_time", invincibleTime);
        registerConfigValue("off_balance_time", offBalanceTime);
        registerConfigValue("off_balance_slow", offBalanceSlow);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }
}
