package com.chen1335.geneChip.chip.chips.survival;
import net.minecraft.network.chat.Component;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import net.minecraft.resources.ResourceLocation;

public class ScrapCollector extends Chip {
    public final JsValueCalculator openExp = expValue("open_exp", "4");
    public final JsValueCalculator extraLootExp = expValue("extra_loot_exp", "5");

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

    @Override
    public Component detailDesc(int lvl) {
        return detailedDesc(extraLootChance.getArgValue(lvl));
    }
}
