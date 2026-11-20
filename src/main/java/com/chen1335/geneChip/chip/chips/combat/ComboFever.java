package com.chen1335.geneChip.chip.chips.combat;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class ComboFever extends Chip {
    public final JsValueCalculator windowKillExp = expValue("window_kill_exp", "10");
    public final JsValueCalculator maxTime = new JsValueCalculator("5");
    public final JsValueCalculator effectTime = new JsValueCalculator("3");

    public ComboFever() {
        super(makeTexture("combo_fever"));
        registerConfigValue("max_time", maxTime);
        registerConfigValue("effect_time", effectTime);
    }

    @Override
    public ChipType getType() {
        return ChipType.COMBAT;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(maxTime.getArgValue(lvl), effectTime.getArgValue(lvl));
    }
}
