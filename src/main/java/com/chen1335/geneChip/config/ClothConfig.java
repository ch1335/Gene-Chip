package com.chen1335.geneChip.config;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.Map;

public class ClothConfig {
    public static void build(ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (mc, parent) -> {
            ConfigBuilder configBuilder = ConfigBuilder.create();

            configBuilder.setTitle(Component.translatable("gene_chip.config"));
            configBuilder.setParentScreen(parent);
            ConfigCategory cardConfig = configBuilder.getOrCreateCategory(Component.translatable("gene_chip.config.card"));

            for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
                entry.getValue().buildClothConfig(entry.getKey().location(),configBuilder, cardConfig);
            }
            return configBuilder.build();
        });

    }
}
