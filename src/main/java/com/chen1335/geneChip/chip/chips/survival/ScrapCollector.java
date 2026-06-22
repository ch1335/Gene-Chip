package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.resources.ResourceLocation;

public class ScrapCollector extends Chip {
    public static final ResourceLocation BONUS_LOOT_TABLE = GeneChip.id("gameplay/scrap_collector_bonus");
    public final JsValueCalculator extraLootChance = new JsValueCalculator("0.2", true);

    public ScrapCollector() {
        super(makeTexture("scrap_collector"));
        registerConfigValue("extra_loot_chance", extraLootChance);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }
}
