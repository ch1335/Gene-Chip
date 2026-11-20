package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.config.GameplayConfig;
import com.chen1335.geneChip.network.ChipLevelUpPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

/**
 * 芯片经验结算的唯一入口（服务端）。
 * <p>
 * 每条经验来源都以「芯片 + 配置项 id」标识：配置项由各芯片类用
 * {@link Chip#expValue(String, String)} 注册（同样是 JS 公式，可在 server.toml 里调整），
 * 结算时按芯片当前等级求值。
 * <p>
 * 规则见 {@code docs/chip-experience-design.md}：只给装备槽中的芯片加经验；不设经验上限、不判挂机；
 * 去重只用于「一次玩家行为不要被重复结算」；升级只发提示，不给属性/奖励。
 */
public final class ChipExperience {
    /** 被动型经验每满 1 分钟结算一次（避免每 tick 发包同步）。 */
    private static final int PASSIVE_WINDOW_TICKS = 20 * 60;
    /** 未升级时经验条同步的最小间隔（2 秒）。 */
    private static final int SYNC_INTERVAL_TICKS = 40;
    /** 去重表的软上限，超过就整体清空（都是瞬时数据，清空只影响一次去重）。 */
    private static final int DEDUPE_MAP_LIMIT = 512;

    private ChipExperience() {
    }

    /** 记一次经验（无去重）。 */
    public static void grant(ServerPlayer player, Chip chip, String sourceId) {
        grant(player, chip, sourceId, null, 0, 1);
    }

    /** 记一次经验，{@code dedupeKey} 相同的事件在 20 tick 内只结算一次。 */
    public static void grant(ServerPlayer player, Chip chip, String sourceId, String dedupeKey) {
        grant(player, chip, sourceId, dedupeKey, 20, 1);
    }

    /** 记一次经验并去重。 */
    public static void grant(ServerPlayer player, Chip chip, String sourceId, String dedupeKey, int dedupeTicks) {
        grant(player, chip, sourceId, dedupeKey, dedupeTicks, 1);
    }

    /** 按数量放大：{@code multiplier} 为次数/点数（例如点燃的敌人数量、消耗的饥饿值）。 */
    public static void grant(ServerPlayer player, Chip chip, String sourceId, int multiplier) {
        grant(player, chip, sourceId, null, 0, multiplier);
    }

    /** 按数量放大并去重。 */
    public static void grant(ServerPlayer player, Chip chip, String sourceId, int multiplier,
                             String dedupeKey, int dedupeTicks) {
        grant(player, chip, sourceId, dedupeKey, dedupeTicks, multiplier);
    }

    private static void grant(ServerPlayer player, Chip chip, String sourceId, String dedupeKey,
                              int dedupeTicks, int multiplier) {
        if (player == null || chip == null || multiplier <= 0 || player.level().isClientSide) return;
        if (!chip.updateable()) return;
        ChipInstance<?> instance = equippedInstance(player, chip);
        if (instance == null) return;

        int amount = amountOf(chip, sourceId, instance.getLvl(), multiplier);
        if (amount <= 0) return;

        PlayerRunTimeData runtime = runtime(player);
        if (dedupeKey != null && dedupeTicks > 0) {
            String key = chipKey(chip) + "|" + sourceId + "|" + dedupeKey;
            long now = player.level().getGameTime();
            Long last = runtime.chipExpDedupe.get(key);
            if (last != null && now - last < dedupeTicks) return;
            if (runtime.chipExpDedupe.size() > DEDUPE_MAP_LIMIT) {
                runtime.chipExpDedupe.clear();
            }
            runtime.chipExpDedupe.put(key, now);
        }
        addAmount(player, chip, amount);
    }

    /**
     * 被动型芯片：由芯片的 tick 每 tick 调用一次，内部累计满 1 分钟才真正加经验。
     * 经验值取自 {@code sourceId} 对应的配置项（例如「每分钟经验」）。
     */
    public static void grantPassive(ServerPlayer player, Chip chip, String sourceId) {
        if (player == null || chip == null || player.level().isClientSide) return;
        if (!chip.updateable()) return;
        ChipInstance<?> instance = equippedInstance(player, chip);
        if (instance == null) return;

        PlayerRunTimeData runtime = runtime(player);
        String key = chipKey(chip) + "|" + sourceId;
        int ticks = runtime.chipExpPassiveTicks.getOrDefault(key, 0) + 1;
        if (ticks < PASSIVE_WINDOW_TICKS) {
            runtime.chipExpPassiveTicks.put(key, ticks);
            return;
        }
        runtime.chipExpPassiveTicks.put(key, 0);

        int amount = amountOf(chip, sourceId, instance.getLvl(), 1);
        if (amount > 0) {
            addAmount(player, chip, amount);
        }
    }

    /** 按芯片等级对配置项里的 JS 公式求值，乘以数量与全局倍率。 */
    private static int amountOf(Chip chip, String sourceId, int lvl, int multiplier) {
        JsValueCalculator calculator = chip.getConfigs().get(sourceId);
        if (calculator == null) {
            return 0;
        }
        try {
            float value = calculator.getValue(lvl) * multiplier * (float) GameplayConfig.getChipExpMultiplier();
            if (!Float.isFinite(value) || value <= 0) {
                return 0;
            }
            return Math.max(1, Math.round(value));
        } catch (RuntimeException exception) {
            // 非法公式不应该让整个事件处理崩掉
            return 0;
        }
    }

    private static void addAmount(ServerPlayer player, Chip chip, int amount) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        ChipInstance<?> instance = data.getChipInfos().getChips()
                .getOrDefault(chip.getType(), Map.of())
                .get(chip);
        if (instance == null) return;

        int oldLevel = instance.getLvl();
        ChipProgression.addExperience(instance, amount);
        int newLevel = instance.getLvl();
        PlayerRunTimeData runtime = runtime(player);
        if (newLevel > oldLevel) {
            // 升级：立即同步经验条 + 发 HUD 提示
            data.syncToClient(player);
            if (GameplayConfig.isChipExpLevelUpNotify()) {
                PacketDistributor.sendToPlayer(player, new ChipLevelUpPacket(chip, newLevel));
            }
        } else {
            // 未升级：节流同步（每 2 秒最多一次全量同步），避免高频经验来源刷同步包
            long now = player.level().getGameTime();
            if (now - runtime.chipExpLastSync >= SYNC_INTERVAL_TICKS) {
                runtime.chipExpLastSync = now;
                data.syncToClient(player);
            }
        }
    }

    /** 装备槽里对应的芯片实例；没装备就返回 null（只有装备中的芯片才有经验）。 */
    private static ChipInstance<?> equippedInstance(ServerPlayer player, Chip chip) {
        PlayerChipData data = player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
        if (!data.getSlotInfos().getCurrent().containsKey(chip)) {
            return null;
        }
        return data.getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip);
    }

    private static PlayerRunTimeData runtime(ServerPlayer player) {
        return player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
    }

    private static String chipKey(Chip chip) {
        ResourceLocation id = RegisterTypes.CHIP.getKey(chip);
        return id == null ? chip.getClass().getName() : id.toString();
    }
}
