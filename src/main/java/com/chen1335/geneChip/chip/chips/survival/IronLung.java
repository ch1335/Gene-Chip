package com.chen1335.geneChip.chip.chips.survival;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;

public class IronLung extends Chip {
    public final JsValueCalculator breathExp = expValue("breath_exp", "2");

    public final JsValueCalculator decreaseAirSupplyMul = new JsValueCalculator("0.5",true);

    public IronLung() {
        super(makeTexture("iron_lung"));
        registerConfigValue("decrease_air_supply_mul", decreaseAirSupplyMul);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(decreaseAirSupplyMul.getArgValue(lvl));
    }

    @Override
    public Component upgradeEffectText(String configId, UpgradeDirection direction) {
        if ("decrease_air_supply_mul".equals(configId)) {
            return Component.translatable("gene_chip.upgrade.iron_lung");
        }
        return super.upgradeEffectText(configId, direction);
    }
}
