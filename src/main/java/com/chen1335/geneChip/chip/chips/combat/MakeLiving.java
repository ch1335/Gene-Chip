package com.chen1335.geneChip.chip.chips.combat;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class MakeLiving extends Chip {
    public final JsValueCalculator recycleExp = expValue("recycle_exp", "3");
    public final JsValueCalculator recyclingChance = new JsValueCalculator("0.2",true);

    public MakeLiving() {
        super(makeTexture("make_living"));
        registerConfigValue("recycling_chance", recyclingChance);
    }

    @Override
    public ChipType getType() {
        return ChipType.COMBAT;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(recyclingChance.getArgValue(lvl));
    }
}
