package com.chen1335.geneChip.chip.chips.combat;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class PrecisionShooting extends Chip {
    public final JsValueCalculator headshotExp = expValue("headshot_exp", "5");
    /** 普通命中（非爆头）的经验。 */
    public final JsValueCalculator hitExp = expValue("hit_exp", "1");

    public final JsValueCalculator damageMul = new JsValueCalculator("0.25", true);

    public PrecisionShooting() {
        super(makeTexture("precision_shooting"));
        registerConfigValue("damage_mul", damageMul);
    }

    @Override
    public ChipType getType() {
        return ChipType.COMBAT;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(damageMul.getArgValue(lvl));
    }
}
