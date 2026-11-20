package com.chen1335.geneChip.chip.chips.survival;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class Endurance extends Chip {
    public final JsValueCalculator hungerExp = expValue("hunger_exp", "2");

    public final JsValueCalculator exhaustionReduce = new JsValueCalculator("0.3",true);

    public Endurance() {
        super(makeTexture("endurance"));
        registerConfigValue("exhaustion_reduce", exhaustionReduce);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(exhaustionReduce.getArgValue(lvl));
    }
}
