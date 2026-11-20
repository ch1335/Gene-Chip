package com.chen1335.geneChip.chip.chips.survival;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class SewagePurificationPack extends Chip {
    public final JsValueCalculator purifyExp = expValue("purify_exp", "5");


    public SewagePurificationPack() {
        super(makeTexture("sewage_purification_pack"));
    }

    @Override
    public boolean updateable() {
        // 效果没有数值（没有可配置算法），不参与升级
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
