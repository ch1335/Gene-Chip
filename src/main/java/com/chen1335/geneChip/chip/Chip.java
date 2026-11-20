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
        return getDesc();
    }

    /**
     * 生成「详细描述」的本地化文本（{@code <命名空间>.chip.<id>.desc.detailed}）。
     * <p>
     * 参数由各芯片在自己的 {@link #detailDesc(int)} 覆写里手动选择，基类不再从
     * {@link #configValue} 自动读取——不是所有配置项都需要展示，顺序也未必与配置注册顺序一致。
     */
    protected Component detailedDesc(Object... args) {
        ResourceKey<Chip> chipResourceKey = RegisterTypes.CHIP.getResourceKey(this).orElseThrow();
        ResourceLocation location = chipResourceKey.location();
        return Component.translatable("%s.chip.%s.desc.detailed".formatted(location.getNamespace(), location.getPath()), args);
    }


    private Component displayId;
    private Component descId;

    /**
     * 是否参与升级（即配置里有没有可调的效果算法）。
     * <p>
     * 返回 {@code false} 的芯片不显示等级与经验条，也不会结算经验——
     * 这类芯片的效果是硬编码/由战利品表实现的，升级没有可改变的数值。
     */
    public boolean updateable() {
        return true;
    }

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

    /**
     * 注册一个「芯片经验」配置项：值同样是 JS 公式（可在 server.toml 里调整），
     * 经验结算时由 {@code ChipExperience} 按当前芯片等级求值。
     */
    protected JsValueCalculator expValue(String id, String defaultFormula) {
        JsValueCalculator calculator = new JsValueCalculator(defaultFormula);
        registerConfigValue(id, calculator);
        return calculator;
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
            // weight 不展示升级箭头；经验值属于「获取速率」而不是芯片效果，也不展示
            if (id.equals("weight") || id.endsWith("_exp")) {
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

    /**
     * 「升级效果」里每一行的文案。默认是「升级增加/降低 + 配置项名」；
     * 文案需要改写成效果本身的芯片可以覆写（例如铁肺写成「升级延长水下呼吸时间」）。
     */
    public Component upgradeEffectText(String configId, UpgradeDirection direction) {
        Component attribute = Component.translatable("gene_chip.config_value.%s".formatted(configId));
        String key = direction == UpgradeDirection.INCREASE
                ? "gene_chip.details.upgrade_increase"
                : "gene_chip.details.upgrade_decrease";
        return Component.translatable(key, attribute);
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
            boolean expValue = id.endsWith("_exp");
            Component label = expValue
                    ? Component.literal(com.chen1335.geneChip.config.ChipParamDescriptions.expLabel(location.getPath(), id))
                    : Component.translatable("gene_chip.config_value.%s".formatted(id));
            @NotNull
            StringListEntry config = entryBuilder.startStrField(label, calculator.getCalculator())
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
