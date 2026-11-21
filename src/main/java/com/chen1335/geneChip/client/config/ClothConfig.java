package com.chen1335.geneChip.client.config;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.chipConfig.ChipConfig;
import com.chen1335.geneChip.config.GameplayConfig;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Map;

/**
 * 客户端专用：注册 Cloth Config 配置界面。
 * 只能在客户端调用（由 {@code ClientBootstrap} 在 dist 判断之后调用）。
 */
public class ClothConfig {
    public static void build(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> {
            ConfigBuilder configBuilder = ConfigBuilder.create();

            configBuilder.setTitle(Component.translatable("gene_chip.config"));
            configBuilder.setParentScreen(parent);
            ConfigCategory gameplayConfig = configBuilder.getOrCreateCategory(Component.translatable("gene_chip.config.gameplay"));
            ConfigCategory cardConfig = configBuilder.getOrCreateCategory(Component.translatable("gene_chip.config.card"));
            ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();

            gameplayConfig.addEntry(entryBuilder.startIntField(Component.translatable("gene_chip.config.max_level"), GameplayConfig.getMaxLevel())
                    .setDefaultValue(GameplayConfig.DEFAULT_MAX_LEVEL)
                    .setMin(1)
                    .setMax(GameplayConfig.MAX_ALLOWED_LEVEL)
                    .setTooltip(Component.translatable("gene_chip.config.max_level.tooltip"))
                    .setSaveConsumer(GameplayConfig::setMaxLevel)
                    .build());
            gameplayConfig.addEntry(entryBuilder.startStrField(Component.translatable("gene_chip.config.required_experience"), GameplayConfig.getRequiredExperienceFormula())
                    .setDefaultValue(GameplayConfig.DEFAULT_FORMULA)
                    .setTooltip(Component.translatable("gene_chip.config.required_experience.tooltip"))
                    .setSaveConsumer(GameplayConfig::setRequiredExperienceFormula)
                    .build());
            gameplayConfig.addEntry(entryBuilder.startBooleanToggle(Component.translatable("gene_chip.config.chip_exp_level_up_notify"), GameplayConfig.isChipExpLevelUpNotify())
                    .setDefaultValue(true)
                    .setTooltip(Component.translatable("gene_chip.config.chip_exp_level_up_notify.tooltip"))
                    .setSaveConsumer(GameplayConfig::setChipExpLevelUpNotify)
                    .build());
            gameplayConfig.addEntry(entryBuilder.startDoubleField(Component.translatable("gene_chip.config.chip_exp_multiplier"), GameplayConfig.getChipExpMultiplier())
                    .setDefaultValue(GameplayConfig.DEFAULT_CHIP_EXP_MULTIPLIER)
                    .setMin(0.0D)
                    .setMax(100.0D)
                    .setTooltip(Component.translatable("gene_chip.config.chip_exp_multiplier.tooltip"))
                    .setSaveConsumer(GameplayConfig::setChipExpMultiplier)
                    .build());

            for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
                ChipClothConfig.addChip(entry.getValue(), entry.getKey().location(), configBuilder, cardConfig);
            }

            // 全局配置项由各自的 saveConsumer 直接写入 TOML，这里只需要保存芯片参数 JSON。
            configBuilder.setSavingRunnable(ChipConfig::save);
            return configBuilder.build();
        });

    }
}
