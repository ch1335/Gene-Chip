package com.chen1335.geneChip.chip.chipConfig;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.config.GameplayConfig;
import net.minecraft.resources.ResourceKey;

import java.util.Map;

/**
 * 芯片参数的读写层。
 * <p>
 * 数据存放在 {@link GameplayConfig} 的 server 配置（{@code config/gene_chip/server.toml} 的
 * {@code [chips.<芯片id>]} 分段）里，不再使用独立的 {@code chip_config.json}。
 * <p>
 * {@link #load()} 在配置加载完成（{@code ModConfigEvent.Loading}）以及配置文件被修改后的自动重载
 * （{@code ModConfigEvent.Reloading}）时调用，因此改 TOML 不需要重启游戏。
 */
public class ChipConfig {
    /** 把配置里的参数应用到已注册的芯片上（服务端与客户端都会执行）。 */
    public static void load() {
        if (!GameplayConfig.SPEC.isLoaded()) {
            return;
        }

        // 配置里有、注册表里没有的芯片：通常是 CHIP_ENTRIES 没同步，打日志便于排查
        for (String chipId : GameplayConfig.CHIP_VALUES.keySet()) {
            if (RegisterTypes.CHIP.get(GeneChip.id(chipId)) == null) {
                GeneChip.LOGGER.warn("Config contains section for unknown chip {}, ignored", chipId);
            }
        }

        for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
            String chipId = entry.getKey().location().getPath();
            Map<String, String> values = GameplayConfig.getChipParams(chipId);
            if (values.isEmpty()) {
                // 注册表里新增的芯片还没加进 CHIP_ENTRIES：本次沿用芯片默认值
                GeneChip.LOGGER.warn("No config section for chip {}, using chip defaults", chipId);
                continue;
            }
            entry.getValue().applyConfig(values);
        }
    }

    /** 把芯片当前参数写回 server 配置并保存。 */
    public static void save() {
        if (!GameplayConfig.SPEC.isLoaded()) {
            return;
        }
        for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
            String chipId = entry.getKey().location().getPath();
            GameplayConfig.setChipParams(chipId, entry.getValue().collectConfig());
        }
        GameplayConfig.SPEC.save();
    }
}
