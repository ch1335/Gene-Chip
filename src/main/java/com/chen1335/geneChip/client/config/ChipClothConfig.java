package com.chen1335.geneChip.client.config;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.config.ChipParamDescriptions;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

/**
 * 客户端专用：为 Cloth Config 界面构建单颗芯片的参数子分类。
 * <p>
 * 这些代码不能放在 {@link Chip} 里——Cloth Config 是客户端模组，
 * 专用服务器上它的类会被 RuntimeDistCleaner 剔除，公共类一旦引用就会在模组构造期崩溃。
 */
public final class ChipClothConfig {
    private ChipClothConfig() {
    }

    public static void addChip(Chip chip, ResourceLocation location, ConfigBuilder configBuilder, ConfigCategory category) {
        ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();
        SubCategoryBuilder subCategory = entryBuilder.startSubCategory(chip.getDisplayName());

        for (Map.Entry<String, JsValueCalculator> entry : chip.getConfigs().entrySet()) {
            String id = entry.getKey();
            JsValueCalculator calculator = entry.getValue();
            boolean expValue = id.endsWith("_exp");
            Component label = expValue
                    ? Component.literal(ChipParamDescriptions.expLabel(location.getPath(), id))
                    : Component.translatable("gene_chip.config_value.%s".formatted(id));
            StringListEntry config = entryBuilder.startStrField(label, calculator.getCalculator())
                    .setDefaultValue(calculator.getDefaultCalculator())
                    .setSaveConsumer(strings -> {
                        calculator.restCalculator();
                        calculator.cleanCapturedValue();
                        calculator.setCalculator(strings);
                    })
                    .build();
            subCategory.add(config);
        }

        category.addEntry(subCategory.build());
    }
}
