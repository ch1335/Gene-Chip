package com.chen1335.geneChip.client;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.chips.tactics.DoubleJump;
import com.chen1335.geneChip.chip.chips.tactics.SlidingTackle;
import com.chen1335.geneChip.network.PlayerActionPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void ClientTickEvent(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 滑铲芯片逻辑
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SLIDING_TACKLE).ifPresent(chipInstance -> {
            boolean isSprinting = player.isSprinting();
            boolean isSneaking = player.isShiftKeyDown();

            if (isSprinting && isSneaking && !GeneChipAPI.isChipCooldown(player, ChipTypes.SLIDING_TACKLE.get())) {
                if (player.getFoodData().getFoodLevel() >= 2) {
                    SlidingTackle chip = chipInstance.getChip();
                    float slideDistance = chip.slideDistance.getValue(chipInstance.getLvl());
                    player.setDeltaMovement(player.getViewVector(0).scale(slideDistance));

                    PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.SLIDING_TACKLE, new CompoundTag()));

                    int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                    GeneChipAPI.addChipCooldown(player, ChipTypes.SLIDING_TACKLE.get(), cooldown);
                }
            }
        });
        
        // 二段跳芯片逻辑
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.DOUBLE_JUMP).ifPresent(chipInstance -> {
            PlayerRunTimeData runtimeData = player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
            
            // 检测跳跃键按下且在空中且可以使用二段跳
            if (Minecraft.getInstance().options.keyJump.isDown() && !player.onGround()) {
                if (!GeneChipAPI.isChipCooldown(player, ChipTypes.DOUBLE_JUMP.get())) {
                    DoubleJump chip = chipInstance.getChip();
                    float saturationCost = chip.saturationCost.getValue(chipInstance.getLvl());
                    
                    // 检查饱和度是否足够
                    if (player.getFoodData().getFoodLevel() >= saturationCost) {
                        // 发送二段跳请求到服务器
                        CompoundTag tag = new CompoundTag();
                        tag.putFloat("saturation_cost", saturationCost);
                        PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.DOUBLE_JUMP, tag));
                        
                        // 客户端立即执行跳跃
                        player.jumpFromGround();
                        
                        // 标记已使用二段跳
                        runtimeData.canDoubleJump = false;
                        
                        // 添加冷却时间
                        int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                        GeneChipAPI.addChipCooldown(player, ChipTypes.DOUBLE_JUMP.get(), cooldown);
                    }
                }
            }
        });
    }

    public static void KeyInputEvent(InputEvent.Key event){

    }
}
