package com.chen1335.geneChip.chip.chips.combat;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class MeleeAttackMaster  extends Chip {
    public final JsValueCalculator hitExp = expValue("hit_exp", "2");
    public final JsValueCalculator killExp = expValue("kill_exp", "6");

    public final JsValueCalculator damageMul = new JsValueCalculator("0.25", true);

    public MeleeAttackMaster() {
        super(makeTexture("melee_attack_master"));
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
