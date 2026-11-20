package com.chen1335.geneChip.chip.chips.special;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class VengefulFlame extends Chip {
    public final JsValueCalculator deathExp = expValue("death_exp", "25");
    public final JsValueCalculator burnExp = expValue("burn_exp", "5");
    public final JsValueCalculator revengeExp = expValue("revenge_exp", "1");

    public final JsValueCalculator explosionRadius = new JsValueCalculator("4");
    public final JsValueCalculator explosionDamage = new JsValueCalculator("30");
    public final JsValueCalculator burnDuration = new JsValueCalculator("5");

    public VengefulFlame() {
        super(makeTexture("vengeful_flame"));
        registerConfigValue("explosion_radius", explosionRadius);
        registerConfigValue("explosion_damage", explosionDamage);
        registerConfigValue("burn_duration", burnDuration);
    }

    @Override
    public ChipType getType() {
        return ChipType.SPECIAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(explosionRadius.getArgValue(lvl), explosionDamage.getArgValue(lvl),
                burnDuration.getArgValue(lvl));
    }
}
