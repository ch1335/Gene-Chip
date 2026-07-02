package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.gui.entries.StringListEntry;
import me.shedaniel.clothconfig2.impl.builders.SubCategoryBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public abstract class Chip {
    public final ResourceLocation texture;
    private final Map<String, JsValueCalculator> configValue = new LinkedHashMap<>();

    private final JsValueCalculator weight = new JsValueCalculator("10");

    public Chip(ResourceLocation texture) {
        this.texture = texture;
        registerConfigValue("weight", weight);
    }

    public float getWeight(Level level) {
        return weight.getValue(0);
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public Component getDesc() {
        if (descId == null) {
            ResourceKey<Chip> chipResourceKey = RegisterTypes.CHIP.getResourceKey(this).orElseThrow();
            ResourceLocation location = chipResourceKey.location();
            descId = Component.translatable("%s.chip.%s.desc".formatted(location.getNamespace(), location.getPath()));
        }
        return descId;
    }

    public Component detailDesc(int lvl) {
        ResourceKey<Chip> chipResourceKey = RegisterTypes.CHIP.getResourceKey(this).orElseThrow();
        ResourceLocation location = chipResourceKey.location();
        Object[] args = configValue.values().stream().map(calculator -> calculator.getArgValue(lvl)).toArray();
        return Component.translatable("%s.chip.%s.desc.detailed".formatted(location.getNamespace(), location.getPath()), args);

    }


    private Component displayId;
    private Component descId;
    private Component detailDescId;

    public Component getDisplayName() {
        if (displayId == null) {
            ResourceKey<Chip> chipResourceKey = RegisterTypes.CHIP.getResourceKey(this).orElseThrow();
            ResourceLocation location = chipResourceKey.location();
            displayId = Component.translatable("%s.chip.%s.name".formatted(location.getNamespace(), location.getPath()));
        }
        return displayId;
    }

    public abstract ChipType getType();

    protected static ResourceLocation makeTexture(String s) {
        return ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "textures/chip/chip_icons/%s.png".formatted(s));
    }

    public void registerConfigValue(String id, JsValueCalculator calculator) {
        configValue.put(id, calculator);
    }

    public void buildClothConfig(ResourceLocation location, ConfigBuilder configBuilder, ConfigCategory configCategory) {
        ConfigEntryBuilder entryBuilder = configBuilder.entryBuilder();
        SubCategoryBuilder subCategoryBuilder = entryBuilder.startSubCategory(getDisplayName());

        configValue.forEach((id, calculator) -> {
            @NotNull
            StringListEntry config = entryBuilder.startStrField(Component.translatable("gene_chip.config_value.%s".formatted(id)), calculator.getCalculator())
                    .setDefaultValue(calculator.getDefaultCalculator())
                    .setSaveConsumer(strings -> {
                        calculator.restCalculator();
                        calculator.setCalculator(strings);
                    })
                    .build();

            subCategoryBuilder.add(config);
        });

        configCategory.addEntry(subCategoryBuilder.build());
    }

    public void onImmunityValueChanged(Player player, ChipInstance<?> instance, int immunityValue) {
    }

    /**
     * 世界因子发生变化（天数切换）时调用。需要响应因子联动的芯片应覆写此方法。
     */
    public void onDayChange(Player player, ChipInstance<?> instance) {
    }

    public void onEquipped(Player player, ChipInstance<?> instance) {

    }

    public void onUnEquipped(Player player, ChipInstance<?> instance) {

    }

    public void tick(Player player, ChipInstance<?> instance) {
    }

    public void applyConfig(Map<String, String> config) {
        config.forEach((id, calculator) -> {
            JsValueCalculator valueCalculator = configValue.get(id);
            if (valueCalculator != null) {
                valueCalculator.cleanCapturedValue();
                valueCalculator.setCalculator(calculator);
            }
        });
    }

    public Map<String, String> collectConfig() {
        Map<String, String> result = new LinkedHashMap<>();
        configValue.forEach((id, calculator) -> {
            String current = calculator.getCalculator();
            result.put(id, current);
        });
        return result;
    }

    public ChipInstance<?> createInstance() {
        return new ChipInstance<>(this, 0, 1);
    }

}
