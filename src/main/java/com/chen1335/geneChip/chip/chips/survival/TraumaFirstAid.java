package com.chen1335.geneChip.chip.chips.survival;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;

public class TraumaFirstAid extends Chip {
    public final JsValueCalculator healItemExp = expValue("heal_item_exp", "5");
    /** 额外恢复的比例：0.5 = 额外恢复原治疗量的 50%。 */
    public final JsValueCalculator extraHealRatio = new JsValueCalculator("0.5", true);

    public TraumaFirstAid() {
        super(makeTexture("trauma_first_aid"));
        registerConfigValue("extra_heal_ratio", extraHealRatio);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(extraHealRatio.getArgValue(lvl));
    }
}
