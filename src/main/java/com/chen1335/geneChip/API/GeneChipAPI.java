package com.chen1335.geneChip.API;

import com.chen1335.geneChip.API.events.ConsumeFoodEvent;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.network.ChipSelectPacket;
import com.immunity.util.ImmunityServerUtil;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;
import org.apache.logging.log4j.util.Cast;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class GeneChipAPI {

    /**
     * 获取玩家的免疫值（仅服务端）
     */
    public static int getImmunityValue(Player player) {
        if (player instanceof ServerPlayer serverPlayer) {
            return ImmunityServerUtil.getImmunity(serverPlayer);
        }
        return 100;
    }

    /**
     * 免疫值变化时通知所有已装备芯片
     */
    public static void onImmunityValueChanged(Player player) {
        int immunityValue = getImmunityValue(player);
        PlayerChipData data = getPlayerChipData(player);
        data.getSlotInfos().currentSlots.forEach((chip, instance) -> {
            chip.onImmunityValueChanged(player, instance, immunityValue);
        });
    }

    public static PlayerChipData getPlayerChipData(Player player){
        if (player.isLocalPlayer()) {
            return GeneChipClient.getPlayerChipData();
        }
        return player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA);
    }

    /**
     * 获取玩家拥有的指定芯片实例，可能为 null
     */
    public static <T extends Chip> ChipInstance<T> getPlayerChip(Player player, T chip) {
        PlayerChipData data = getPlayerChipData(player);
        return Cast.cast(data.getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip));
    }

    /**
     * 获取玩家当前装备槽中指定芯片的实例（Supplier 变体），返回 Optional
     */
    public static <T extends Chip> Optional<ChipInstance<T>> getPlayerEquippedChip(Player player, Supplier<T> chip) {
        PlayerChipData data = getPlayerChipData(player);
        return Optional.ofNullable(Cast.cast(data.getSlotInfos().getCurrent().get(chip.get())));
    }

    /**
     * 获取玩家运行时数据
     */
    public static PlayerRunTimeData getPlayerRunTimeData(Player player) {
        return player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
    }

    /**
     * 为芯片添加冷却时间（tick 为单位）
     */
    public static void addChipCooldown(Player player, Chip chip, int tick) {
        PlayerChipData data = getPlayerChipData(player);
        data.addCoolDown(chip, tick);
    }

    /**
     * 检查芯片是否处于冷却中
     */
    public static boolean isChipCooldown(Player player, Chip chip) {
        PlayerChipData data = getPlayerChipData(player);
        return data.getCoolDownInfos().isCoolDown(chip);
    }

    public static boolean addChipExperience(ServerPlayer player, Chip chip, int amount) {
        return player.getData(GCAttachmentTypes.PLAYER_CHIP_DATA).addChipExperience(player, chip, amount);
    }

    /**
     * 设置指定槽位的芯片，自动处理旧芯片卸下和新芯片装备回调，重新烘焙 currentSlots
     */
    public static void setSlotChip(Player player, ChipSlot slot) {
        PlayerChipData data = getPlayerChipData(player);
        IntObjectMap<ChipSlot> slots = data.getSlotInfos().getSlots();
        Map<Chip, ChipInstance<?>> currentSlots = data.getSlotInfos().currentSlots;
        Optional<ChipInstance<?>> optional = slot.instance();
        if (optional.isPresent()) {
            ChipInstance<?> instance = optional.get();
            if (!currentSlots.containsKey(instance.getChip())) {
                if (slot.index() < slots.size()) {
                    ChipSlot oldSlot = slots.get(slot.index());
                    oldSlot.instance().ifPresent(oldInstance -> oldInstance.getChip().onUnEquipped(player, oldInstance));
                    slots.put(slot.index(), slot);
                    instance.getChip().onEquipped(player, instance);
                }
                data.getSlotInfos().bakeCurrent();
            }
        } else {
            if (slot.index() < slots.size()) {
                ChipSlot oldSlot = slots.get(slot.index());
                oldSlot.instance().ifPresent(oldInstance -> oldInstance.getChip().onUnEquipped(player, oldInstance));
                slots.put(slot.index(), slot);
            }
            data.getSlotInfos().bakeCurrent();
        }
    }

    //开始一次卡牌选择
    public static void StartCardSelect(ServerPlayer serverPlayer,List<ChipInstance<?>> candidates){
            PacketDistributor.sendToPlayer(serverPlayer,new ChipSelectPacket(candidates));
    }

    public static void consumeFood(Player player, int food, ChipInstance<?> instance){
        ConsumeFoodEvent post = NeoForge.EVENT_BUS.post(new ConsumeFoodEvent(player, food, instance));
        if (!post.isCanceled()) {
            player.getFoodData().eat(-food,0);
        }
    }
}
