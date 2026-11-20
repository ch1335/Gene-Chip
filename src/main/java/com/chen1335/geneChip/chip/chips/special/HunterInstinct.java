package com.chen1335.geneChip.chip.chips.special;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class HunterInstinct extends Chip {
    public final JsValueCalculator markExp = expValue("mark_exp", "5");
    public final JsValueCalculator markDeathExp = expValue("mark_death_exp", "10");

    public final JsValueCalculator markDuration = new JsValueCalculator("15");
    public final JsValueCalculator damageBoost = new JsValueCalculator("0.15",true);
    public final JsValueCalculator hungerCost = new JsValueCalculator("5");
    public final JsValueCalculator cooldown = new JsValueCalculator("20");

    public HunterInstinct() {
        super(makeTexture("hunter_instinct"));
        registerConfigValue("hunger_cost", hungerCost);
        registerConfigValue("damage_boost", damageBoost);
        registerConfigValue("mark_duration", markDuration);
        registerConfigValue("cooldown", cooldown);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(hungerCost.getArgValue(lvl), damageBoost.getArgValue(lvl), markDuration.getArgValue(lvl), cooldown.getArgValue(lvl));
    }
}
