package com.chen1335.geneChip.chip.chips.tactics;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class DoubleJump extends Chip {
    public final JsValueCalculator jumpExp = expValue("jump_exp", "3");

    public final JsValueCalculator saturationCost = new JsValueCalculator("2");
    public final JsValueCalculator exhaustionDuration = new JsValueCalculator("2");
    public final JsValueCalculator cooldown = new JsValueCalculator("5");

    public DoubleJump() {
        super(makeTexture("double_jump"));
        registerConfigValue("saturation_cost", saturationCost);
        registerConfigValue("exhaustion_duration", exhaustionDuration);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.TACTICS;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(saturationCost.getArgValue(lvl), exhaustionDuration.getArgValue(lvl), cooldown.getArgValue(lvl));
    }
}
