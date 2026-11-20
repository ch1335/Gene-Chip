package com.chen1335.geneChip.chip.chips.survival;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class WildHunter extends Chip {
    public final JsValueCalculator animalKillExp = expValue("animal_kill_exp", "5");

    public WildHunter() {
        super(makeTexture("wild_hunter"));
    }

    @Override
    public boolean updateable() {
        // 额外掉落由战利品表实现（没有可配置算法），不参与升级
        return false;
    }


    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc();
    }
}
