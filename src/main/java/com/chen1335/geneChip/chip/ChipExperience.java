package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.config.GameplayConfig;
import com.chen1335.geneChip.network.ChipLevelUpPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Map;

/**
 * 芯片经验结算的唯一入口（服务端）。
 * <p>
 * 规则见 {@code docs/chip-experience-design.md}：
 * <ul>
 *   <li>只给**装备槽中**的芯片加经验；</li>
 *   <li>不设经验上限、不判挂机；</li>
 *   <li>去重只用于「一次玩家行为不要被重复结算」（例如霰弹多弹丸、同一 tick 多路径）；</li>
 *   <li>升级只发提示，不给属性/奖励。</li>
 * </ul>
 * 所有调用点都必须位于服务端逻辑中（客户端那份 {@link PlayerChipData} 加了也会被覆盖）。
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
    public static void grant(ServerPlayer player, Chip chip, int amount) {
        grant(player, chip, amount, null, 0);
    }

    /** 记一次经验，{@code dedupeKey} 相同的事件在 {@code dedupeTicks} 内只结算一次（默认 20 tick）。 */
    public static void grant(ServerPlayer player, Chip chip, int amount, String dedupeKey) {
        grant(player, chip, amount, dedupeKey, 20);
    }

    /**
     * @param dedupeKey   为 null 表示不去重；典型用法是按目标 UUID 去重（`"hit:" + entity.getUUID()`）
     * @param dedupeTicks 去重窗口（tick）
     */
    public static void grant(ServerPlayer player, Chip chip, int amount, String dedupeKey, int dedupeTicks) {
        if (player == null || chip == null || amount <= 0) return;
        if (player.level().isClientSide) return;

        PlayerRunTimeData runtime = runtime(player);
        if (dedupeKey != null && dedupeTicks > 0) {
            String key = chipKey(chip) + "|" + dedupeKey;
            long now = player.level().getGameTime();
            Long last = runtime.chipExpDedupe.get(key);
            if (last != null && now - last < dedupeTicks) return;
            if (runtime.chipExpDedupe.size() > DEDUPE_MAP_LIMIT) {
                runtime.chipExpDedupe.clear();
            }
            runtime.chipExpDedupe.put(key, now);
        }
        add(player, chip, scale(amount));
    }

    /**
     * 被动型芯片：由芯片的 tick 每 tick 调用一次，内部累计满 1 分钟才真正加经验。
     *
     * @param amountPerMinute 有效状态下每分钟应获得的经验
     */
    public static void grantPassive(ServerPlayer player, Chip chip, int amountPerMinute) {
        if (player == null || chip == null || amountPerMinute <= 0) return;
        if (player.level().isClientSide) return;
        if (!isEquipped(player, chip)) return;

        PlayerRunTimeData runtime = runtime(player);
        String key = chipKey(chip);
        int ticks = runtime.chipExpPassiveTicks.getOrDefault(key, 0) + 1;
        if (ticks < PASSIVE_WINDOW_TICKS) {
            runtime.chipExpPassiveTicks.put(key, ticks);
            return;
        }
        runtime.chipExpPassiveTicks.put(key, 0);
        add(player, chip, scale(amountPerMinute));
    }

    private static void add(ServerPlayer player, Chip chip, int amount) {
        if (amount <= 0 || !isEquipped(player, chip)) return;

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

    private static boolean isEquipped(ServerPlayer player, Chip chip) {
        return player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA)
                .getSlotInfos().getCurrent().containsKey(chip);
    }

    private static PlayerRunTimeData runtime(ServerPlayer player) {
        return player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
    }

    private static String chipKey(Chip chip) {
        ResourceLocation id = RegisterTypes.CHIP.getKey(chip);
        return id == null ? chip.getClass().getName() : id.toString();
    }

    private static int scale(int amount) {
        int scaled = (int) Math.round(amount * GameplayConfig.getChipExpMultiplier());
        return Math.max(0, scaled);
    }
}
