package com.chen1335.geneChip.chip.chips.combat;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class HeadShotHunter extends Chip {
    public final JsValueCalculator critExp = expValue("crit_exp", "5");
    public final JsValueCalculator killChance = new JsValueCalculator("0.3",true);

    public HeadShotHunter() {
        super(makeTexture("head_shot_hunter"));
        registerConfigValue("kill_chance", killChance);
    }

    @Override
    public ChipType getType() {
        return ChipType.COMBAT;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(killChance.getArgValue(lvl));
    }
}
