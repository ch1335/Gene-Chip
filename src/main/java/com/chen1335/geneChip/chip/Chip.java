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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class Chip {
    public final ResourceLocation texture;
    private final Map<String, JsValueCalculator> configValue = new LinkedHashMap<>();
    private final Map<String, UpgradeSemantic> upgradeSemantics = new LinkedHashMap<>();

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
        List<Object> objects = new ArrayList<>();
        configValue.forEach((key, jsValueCalculator) -> {
            if (!key.equals("weight")) {
                objects.add(jsValueCalculator.getArgValue(lvl));
            }
        });
        Object[] args = objects.toArray();
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
        registerConfigValue(id, calculator, defaultUpgradeSemantic(id));
    }

    public void registerConfigValue(String id, JsValueCalculator calculator, UpgradeSemantic semantic) {
        configValue.put(id, calculator);
        upgradeSemantics.put(id, semantic);
    }

    public List<UpgradeEffect> getUpgradeEffects(int currentLevel, int maxLevel) {
        if (maxLevel <= 1 || currentLevel >= maxLevel) {
            return List.of();
        }

        int level = Math.max(1, currentLevel);
        List<UpgradeEffect> effects = new ArrayList<>();
        for (Map.Entry<String, JsValueCalculator> entry : configValue.entrySet()) {
            String id = entry.getKey();
            if (id.equals("weight")) {
                continue;
            }

            try {
                float currentValue = entry.getValue().getValue(level);
                float nextValue = entry.getValue().getValue(level + 1);
                if (!Float.isFinite(currentValue) || !Float.isFinite(nextValue)) {
                    continue;
                }

                UpgradeSemantic semantic = upgradeSemantics.getOrDefault(id, UpgradeSemantic.NUMERIC);
                UpgradeDirection direction;
                if (Math.abs(nextValue - currentValue) > 1.0E-6F) {
                    UpgradeDirection rawDirection = nextValue > currentValue
                            ? UpgradeDirection.INCREASE
                            : UpgradeDirection.DECREASE;
                    direction = semantic == UpgradeSemantic.LOWER_IS_BETTER
                            ? rawDirection.opposite()
                            : rawDirection;
                } else {
                    // Current calculators use fixed baseline values. Their semantic still
                    // tells the player the expected direction of a future level upgrade.
                    direction = semantic == UpgradeSemantic.LOWER_IS_BETTER
                            ? UpgradeDirection.DECREASE
                            : UpgradeDirection.INCREASE;
                }
                effects.add(new UpgradeEffect(id, direction));
            } catch (RuntimeException ignored) {
                // Invalid user formulas should not prevent the details screen from opening.
            }
        }
        return effects;
    }

    private static UpgradeSemantic defaultUpgradeSemantic(String id) {
        return switch (id) {
            case "cooldown", "saturation_cost", "food_cost", "hunger_cost", "immunity_cost",
                 "exhaustion_duration", "off_balance_time", "off_balance_slow",
                 "quality_threshold" -> UpgradeSemantic.LOWER_IS_BETTER;
            case "threshold", "decrease_air_supply_mul" -> UpgradeSemantic.HIGHER_IS_BETTER;
            default -> {
                if (id.endsWith("_chance") || id.endsWith("_amount") || id.endsWith("_bonus")
                        || id.endsWith("_mul") || id.endsWith("_reduction") || id.endsWith("_duration")
                        || id.endsWith("_distance") || id.endsWith("_time") || id.endsWith("_radius")
                        || id.endsWith("_speed") || id.endsWith("_window") || id.endsWith("_resistance")
                        || id.equals("food_add") || id.equals("heal") || id.equals("heal_on_kill")) {
                    yield UpgradeSemantic.HIGHER_IS_BETTER;
                }
                yield UpgradeSemantic.NUMERIC;
            }
        };
    }

    public record UpgradeEffect(String configId, UpgradeDirection direction) {
    }

    public enum UpgradeDirection {
        INCREASE,
        DECREASE;

        public UpgradeDirection opposite() {
            return this == INCREASE ? DECREASE : INCREASE;
        }
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
                        calculator.cleanCapturedValue();
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

    public Map<String, JsValueCalculator> getConfigs() {
        return configValue;
    }
    public ChipInstance<?> createInstance() {
        return new ChipInstance<>(this, 0, 1);
    }

}
