package com.chen1335.geneChip.config;

import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.chipConfig.ChipConfig;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraft.util.Mth;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 模组的唯一 server 配置（NeoForge 配置系统 / NightConfig / ModConfigSpec），
 * 落地为 {@code config/gene_chip/server.toml}（世界目录的 {@code serverconfig/gene_chip/server.toml} 可覆盖它）。
 * <p>
 * 内容：
 * <ul>
 *   <li>全局配置：{@code [gameplay]} 与 {@code [chip_experience]}；</li>
 *   <li>每颗芯片的参数（原 {@code chip_config.json}）：{@code [chips.<芯片id>]}，值为 JS 公式。</li>
 * </ul>
 * 使用 server 类型后：文件由服务端加载并同步给客户端；文件被修改时由 NeoForge 的配置监听自动触发
 * {@link ModConfigEvent.Reloading}，本类在事件里重新把芯片参数应用到芯片上（无需重启）。
 */
public final class GameplayConfig {
    public static final String DEFAULT_FORMULA = "100 * lvl";
    public static final int DEFAULT_MAX_LEVEL = 10;
    public static final int MAX_ALLOWED_LEVEL = 1000;
    public static final double DEFAULT_CHIP_EXP_MULTIPLIER = 1.0D;

    /** 配置文件相对 {@code config/} 的路径。 */
    public static final String CONFIG_FILE_NAME = "gene_chip/server.toml";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIRECTORY = FMLPaths.CONFIGDIR.get().resolve("gene_chip");
    private static final Path SERVER_TOML_FILE = CONFIG_DIRECTORY.resolve("server.toml");
    /** 上一版（COMMON 类型）的配置文件，迁移时整份拷贝成 server.toml。 */
    private static final Path LEGACY_COMMON_TOML_FILE = CONFIG_DIRECTORY.resolve("gameplay_config.toml");

    // ---------------------------------------------------------------- 配置项

    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.IntValue MAX_LEVEL;
    public static final ModConfigSpec.ConfigValue<String> REQUIRED_EXPERIENCE;
    public static final ModConfigSpec.BooleanValue CHIP_EXP_LEVEL_UP_NOTIFY;
    public static final ModConfigSpec.DoubleValue CHIP_EXP_MULTIPLIER;

    /** 芯片 id -> 参数 id -> 配置值（原 chip_config.json 的等价物）。 */
    public static final Map<String, Map<String, ModConfigSpec.ConfigValue<String>>> CHIP_VALUES;
    /** 芯片 id -> 参数 id -> 默认公式（用于判断配置是否被改过）。 */
    public static final Map<String, Map<String, String>> CHIP_DEFAULTS;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        builder.comment(
                        "游戏全局配置")
                .push("gameplay");
        MAX_LEVEL = builder
                .comment(
                        "芯片最高等级（1~1000）。",
                        "升级所需的经验由 required_experience 决定。")
                .defineInRange("max_level", DEFAULT_MAX_LEVEL, 1, MAX_ALLOWED_LEVEL);
        REQUIRED_EXPERIENCE = builder
                .comment(
                        "每级升级所需经验公式，变量 lvl = 当前等级，例如 \"100 * lvl\"。",
                        "允许的字符：数字、空格、+ - * / ( ) . 以及 l L v V；公式非法时回退到默认值。")
                .define("required_experience", DEFAULT_FORMULA);
        builder.pop();

        builder.comment(
                        "芯片经验相关")
                .push("chip_experience");
        CHIP_EXP_LEVEL_UP_NOTIFY = builder
                .comment(
                        "芯片升级时是否在 HUD 显示提示。",
                        "升级只给提示，不提供任何属性或物品奖励。")
                .define("level_up_notify", true);
        CHIP_EXP_MULTIPLIER = builder
                .comment(
                        "芯片经验全局倍率（0~100，默认 1.0）。",
                        "调高会加快所有芯片的升级速度；它只是一个倍率旋钮，不是额外的经验加成机制。")
                .defineInRange("multiplier", DEFAULT_CHIP_EXP_MULTIPLIER, 0.0D, 100.0D);
        builder.pop();

        builder.comment(
                        "芯片参数（值为 JS 公式，变量 lvl = 芯片等级）。",
                        "每颗芯片一个分段；把某个参数改坏会导致该芯片计算异常，建议保留默认值。")
                .push("chips");
        Map<String, Map<String, ModConfigSpec.ConfigValue<String>>> chipValues = new LinkedHashMap<>();
        Map<String, Map<String, String>> chipDefaults = new LinkedHashMap<>();
        for (ChipTypes.ChipEntry entry : ChipTypes.CHIP_ENTRIES) {
            Chip probe;
            try {
                probe = entry.factory().get();
            } catch (Throwable throwable) {
                GeneChip.LOGGER.error("Failed to instantiate chip {} for config generation", entry.id(), throwable);
                continue;
            }
            Map<String, ModConfigSpec.ConfigValue<String>> params = new LinkedHashMap<>();
            Map<String, String> defaults = new LinkedHashMap<>();
            builder.comment(
                            "芯片「" + entry.chineseName() + "」（" + entry.id() + "）")
                    .push(entry.id());
            for (Map.Entry<String, JsValueCalculator> configEntry : probe.getConfigs().entrySet()) {
                String parameterId = configEntry.getKey();
                String defaultFormula = configEntry.getValue().getCalculator();
                params.put(parameterId, builder
                        .comment(ChipParamDescriptions.describe(entry.id(), parameterId))
                        .define(parameterId, defaultFormula));
                defaults.put(parameterId, defaultFormula);
            }
            builder.pop();
            chipValues.put(entry.id(), params);
            chipDefaults.put(entry.id(), defaults);
        }
        builder.pop();

        SPEC = builder.build();
        CHIP_VALUES = Map.copyOf(chipValues);
        CHIP_DEFAULTS = Map.copyOf(chipDefaults);
    }

    private GameplayConfig() {
    }

    // ---------------------------------------------------------------- 全局配置读写

    private static boolean loaded() {
        return SPEC.isLoaded();
    }

    public static int getMaxLevel() {
        return loaded() ? Mth.clamp(MAX_LEVEL.get(), 1, MAX_ALLOWED_LEVEL) : DEFAULT_MAX_LEVEL;
    }

    /**
     * 当前经验公式；非法公式自动回退到默认值。
     * 校验结果按「最大等级 + 公式」缓存，避免每次读取都跑一遍 JS。
     */
    public static String getRequiredExperienceFormula() {
        if (!loaded()) {
            return DEFAULT_FORMULA;
        }
        String formula = REQUIRED_EXPERIENCE.get();
        if (formula == null || formula.isBlank()) {
            return DEFAULT_FORMULA;
        }
        String cacheKey = getMaxLevel() + "|" + formula;
        if (!cacheKey.equals(validationCacheKey)) {
            validationCacheKey = cacheKey;
            validationCacheValue = isValidFormula(formula);
        }
        return validationCacheValue ? formula : DEFAULT_FORMULA;
    }

    public static boolean isChipExpLevelUpNotify() {
        return !loaded() || CHIP_EXP_LEVEL_UP_NOTIFY.get();
    }

    public static double getChipExpMultiplier() {
        return loaded() ? CHIP_EXP_MULTIPLIER.get() : DEFAULT_CHIP_EXP_MULTIPLIER;
    }

    public static void setMaxLevel(int value) {
        if (!loaded()) return;
        MAX_LEVEL.set(Mth.clamp(value, 1, MAX_ALLOWED_LEVEL));
        SPEC.save();
    }

    public static void setRequiredExperienceFormula(String formula) {
        if (!loaded() || formula == null || !isValidFormula(formula)) {
            return;
        }
        REQUIRED_EXPERIENCE.set(formula);
        SPEC.save();
    }

    public static void setChipExpLevelUpNotify(boolean value) {
        if (!loaded()) return;
        CHIP_EXP_LEVEL_UP_NOTIFY.set(value);
        SPEC.save();
    }

    public static void setChipExpMultiplier(double value) {
        if (!loaded()) return;
        CHIP_EXP_MULTIPLIER.set(Mth.clamp(value, 0.0D, 100.0D));
        SPEC.save();
    }

    // ---------------------------------------------------------------- 芯片参数读写

    /** 读取某颗芯片当前配置的全部参数（只在配置里声明过的参数）。 */
    public static Map<String, String> getChipParams(String chipId) {
        if (!loaded()) {
            return Map.of();
        }
        Map<String, ModConfigSpec.ConfigValue<String>> params = CHIP_VALUES.get(chipId);
        if (params == null) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        params.forEach((parameterId, value) -> result.put(parameterId, value.get()));
        return result;
    }

    /** 把某颗芯片的参数写回配置（未知参数会被忽略）。 */
    public static void setChipParams(String chipId, Map<String, String> params) {
        if (!loaded() || params == null) {
            return;
        }
        Map<String, ModConfigSpec.ConfigValue<String>> values = CHIP_VALUES.get(chipId);
        if (values == null) {
            return;
        }
        params.forEach((parameterId, formula) -> {
            ModConfigSpec.ConfigValue<String> value = values.get(parameterId);
            if (value != null && formula != null) {
                value.set(formula);
            }
        });
    }

    // ---------------------------------------------------------------- 配置加载 / 重载

    /** 配置首次加载完成（服务端启动或客户端收到服务端同步的配置）。 */
    public static void onConfigLoading(ModConfigEvent.Loading event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        onConfigAvailable();
    }

    /** 配置文件被修改后自动重载（NeoForge 的 ConfigWatcher 监听 TOML 变更）。 */
    public static void onConfigReloading(ModConfigEvent.Reloading event) {
        if (event.getConfig().getSpec() != SPEC) {
            return;
        }
        onConfigAvailable();
    }

    private static void onConfigAvailable() {
        if (!SPEC.isLoaded()) {
            return;
        }
        migrateLegacyConfigs();
        // 重新把配置里的芯片参数应用到芯片上（会清掉 JS 计算缓存）
        ChipConfig.load();
    }

    // ---------------------------------------------------------------- 旧配置迁移

    private static boolean legacyJsonMigrationPending = false;
    private static String validationCacheKey = null;
    private static boolean validationCacheValue = false;

    /**
     * 必须在注册配置之前调用（mod 构造期）：
     * 创建配置目录；如果存在上一版 COMMON 配置且还没有 server.toml，则整份拷贝并改名。
     */
    public static void prepareLegacyMigration() {
        try {
            Files.createDirectories(CONFIG_DIRECTORY);
            boolean hadLegacyCommonToml = Files.exists(LEGACY_COMMON_TOML_FILE);
            if (!Files.exists(SERVER_TOML_FILE) && hadLegacyCommonToml) {
                Files.copy(LEGACY_COMMON_TOML_FILE, SERVER_TOML_FILE);
                Files.move(LEGACY_COMMON_TOML_FILE,
                        LEGACY_COMMON_TOML_FILE.resolveSibling("gameplay_config.toml.migrated"),
                        StandardCopyOption.REPLACE_EXISTING);
                GeneChip.LOGGER.info("Migrated legacy common config to {}", SERVER_TOML_FILE);
            }
            // 只有「新/旧 TOML 都不存在」时，才把更老的 JSON 当作唯一来源迁移
            legacyJsonMigrationPending = !Files.exists(SERVER_TOML_FILE) && !hadLegacyCommonToml
                    && (Files.exists(GeneChip.GAMEPLAY_CONFIG) || Files.exists(GeneChip.CHIP_CONFIG));
        } catch (Exception exception) {
            legacyJsonMigrationPending = false;
            GeneChip.LOGGER.error("Failed to prepare config directory {}", CONFIG_DIRECTORY, exception);
        }
    }

    /** 配置可用后执行迁移：老的 JSON 配置、以及「TOML 里芯片参数还是默认值」时补迁移旧 chip_config.json。 */
    private static void migrateLegacyConfigs() {
        boolean migrated = false;
        if (legacyJsonMigrationPending) {
            legacyJsonMigrationPending = false;
            migrated = migrateLegacyGameplayJson();
            migrated |= migrateLegacyChipJson();
        } else if (Files.exists(GeneChip.CHIP_CONFIG) && chipsAtDefaults()) {
            migrated = migrateLegacyChipJson();
        }
        if (migrated) {
            SPEC.save();
        }
    }

    private static boolean migrateLegacyGameplayJson() {
        Path legacy = GeneChip.GAMEPLAY_CONFIG;
        if (!Files.exists(legacy)) {
            return false;
        }
        try {
            LegacyGameplayData data = GSON.fromJson(Files.readString(legacy), LegacyGameplayData.class);
            if (data != null) {
                if (data.maxLevel != null) {
                    MAX_LEVEL.set(Mth.clamp(data.maxLevel, 1, MAX_ALLOWED_LEVEL));
                }
                if (data.requiredExperience != null && isValidFormula(data.requiredExperience)) {
                    REQUIRED_EXPERIENCE.set(data.requiredExperience);
                }
                if (data.chipExpLevelUpNotify != null) {
                    CHIP_EXP_LEVEL_UP_NOTIFY.set(data.chipExpLevelUpNotify);
                }
                if (data.chipExpMultiplier != null) {
                    CHIP_EXP_MULTIPLIER.set(Mth.clamp(data.chipExpMultiplier, 0.0D, 100.0D));
                }
            }
            Files.move(legacy, legacy.resolveSibling("gameplay_config.json.migrated"), StandardCopyOption.REPLACE_EXISTING);
            GeneChip.LOGGER.info("Migrated legacy gameplay config {}", legacy);
            return true;
        } catch (Exception exception) {
            GeneChip.LOGGER.error("Failed to migrate legacy gameplay config {}", legacy, exception);
            return false;
        }
    }

    private static boolean migrateLegacyChipJson() {
        Path legacy = GeneChip.CHIP_CONFIG;
        if (!Files.exists(legacy)) {
            return false;
        }
        try {
            Map<String, Map<String, String>> data = GSON.fromJson(Files.readString(legacy),
                    new TypeToken<Map<String, Map<String, String>>>() {
                    }.getType());
            if (data != null) {
                data.forEach(GameplayConfig::setChipParams);
            }
            Files.move(legacy, legacy.resolveSibling("chip_config.json.migrated"), StandardCopyOption.REPLACE_EXISTING);
            GeneChip.LOGGER.info("Migrated legacy chip config {}", legacy);
            return true;
        } catch (Exception exception) {
            GeneChip.LOGGER.error("Failed to migrate legacy chip config {}", legacy, exception);
            return false;
        }
    }

    /** 芯片参数是否全部还是默认值（用于判断要不要再迁移一次旧的 chip_config.json）。 */
    public static boolean chipsAtDefaults() {
        if (!loaded()) {
            return true;
        }
        for (Map.Entry<String, Map<String, String>> entry : CHIP_DEFAULTS.entrySet()) {
            Map<String, String> current = getChipParams(entry.getKey());
            for (Map.Entry<String, String> parameter : entry.getValue().entrySet()) {
                if (!parameter.getValue().equals(current.get(parameter.getKey()))) {
                    return false;
                }
            }
        }
        return true;
    }

    // ---------------------------------------------------------------- 经验公式

    public static int requiredExperience(int level) {
        int safeLevel = Mth.clamp(level, 1, Math.max(1, getMaxLevel() - 1));
        try {
            String expression = getRequiredExperienceFormula().replace("lvl", Integer.toString(safeLevel));
            double value = Double.parseDouble(GeneChip.JS_ENGINE.eval(expression).toString());
            if (!Double.isFinite(value) || value < 1) return defaultRequiredExperience(safeLevel);
            return (int) Math.min(Integer.MAX_VALUE, Math.ceil(value));
        } catch (Exception exception) {
            return defaultRequiredExperience(safeLevel);
        }
    }

    public static boolean isValidFormula(String formula) {
        if (formula == null || formula.isBlank() || !formula.matches("[0-9+*/(). _lLvV-]+")) return false;
        for (int level = 1; level < getMaxLevel(); level++) {
            try {
                String expression = formula.replace("lvl", Integer.toString(level));
                double value = Double.parseDouble(GeneChip.JS_ENGINE.eval(expression).toString());
                if (!Double.isFinite(value) || value < 1) return false;
            } catch (Exception exception) {
                return false;
            }
        }
        return true;
    }

    private static int defaultRequiredExperience(int level) {
        return Math.min(Integer.MAX_VALUE, Math.max(1, level * 100));
    }

    private record LegacyGameplayData(Integer maxLevel, String requiredExperience,
                                      Boolean chipExpLevelUpNotify, Double chipExpMultiplier) {
    }
}
