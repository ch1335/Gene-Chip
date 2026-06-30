package com.chen1335.geneChip.client;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.GCAttachmentTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.chips.tactics.DoubleJump;
import com.chen1335.geneChip.chip.chips.tactics.SlidingTackle;
import com.chen1335.geneChip.chip.chips.tactics.TacticalRoll;
import com.chen1335.geneChip.client.animation.AnimationHandler;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.chen1335.geneChip.network.PlayerActionPacket;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    private static long lastBackKeyPress = 0;
    private static final int DOUBLE_TAP_THRESHOLD = 400;

    @SubscribeEvent
    public static void ClientTickEvent(ClientTickEvent.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;
        GeneChipClient.getPlayerChipData().tick(player);
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);

        // 滑铲芯片逻辑
        GeneChipClient.getPlayerEquippedChip(ChipTypes.SLIDING_TACKLE).ifPresent(chipInstance -> {
            boolean isSprinting = player.isSprinting();
            boolean isSneaking = player.isShiftKeyDown();

            if (isSprinting && isSneaking && !GeneChipAPI.isChipCooldown(player, ChipTypes.SLIDING_TACKLE.get())) {
                if (player.getFoodData().getFoodLevel() >= 2) {
                    SlidingTackle chip = chipInstance.getChip();
                    float slideDistance = chip.slideDistance.getValue(chipInstance.getLvl());
                    // 丧尸暴动联动：滑铲距离+2格
                    if (WorldFactorSynergy.isZombieRiot()) {
                        slideDistance += 2;
                    }
                    player.setDeltaMovement(player.getViewVector(0).scale(slideDistance));

                    PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.SLIDING_TACKLE, new CompoundTag()));

                    int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                    GeneChipAPI.addChipCooldown(player, ChipTypes.SLIDING_TACKLE.get(), cooldown);
                    AnimationHandler.playAnimationAndDistribute(player, ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "sliding_tackle"));
                }
            }
        });
    }

    // 战术翻滚芯片逻辑 - 双击后退键
    @SubscribeEvent
    public static void KeyInputEvent(InputEvent.Key event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 只检测按键按下事件
        if (event.getAction() != InputConstants.PRESS) return;

        // 检测后退键（S键）
        KeyMapping keyDown = Minecraft.getInstance().options.keyDown;
        if (event.getKey() == keyDown.getKey().getValue()) {
            GeneChipClient.getPlayerEquippedChip(ChipTypes.TACTICAL_ROLL).ifPresent(chipInstance -> {
                PlayerRunTimeData runtimeData = player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);

                if (!GeneChipAPI.isChipCooldown(player, ChipTypes.TACTICAL_ROLL.get()) && !runtimeData.tacticalRolling) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastBackKeyPress < DOUBLE_TAP_THRESHOLD) {
                        TacticalRoll chip = chipInstance.getChip();
                        float rollDistance = chip.rollDistance.getValue(chipInstance.getLvl()) * 0.5F;

                        player.setDeltaMovement(player.getViewVector(0).multiply(1, 0, 1).scale(-rollDistance));

                        PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.TACTICAL_ROLL, new CompoundTag()));

                        int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                        GeneChipAPI.addChipCooldown(player, ChipTypes.TACTICAL_ROLL.get(), cooldown);
                    }
                    lastBackKeyPress = currentTime;
                }
            });
        }

        if (event.getKey() == Minecraft.getInstance().options.keyJump.getKey().getValue()) {
            // 二段跳芯片逻辑
            GeneChipClient.getPlayerEquippedChip(ChipTypes.DOUBLE_JUMP).ifPresent(chipInstance -> {
                PlayerRunTimeData runtimeData = player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
                if (!player.onGround()) {
                    if (!GeneChipAPI.isChipCooldown(player, ChipTypes.DOUBLE_JUMP.get())) {
                        DoubleJump chip = chipInstance.getChip();
                        float saturationCost = chip.saturationCost.getValue(chipInstance.getLvl());

                        if (player.getFoodData().getFoodLevel() >= saturationCost) {
                            CompoundTag tag = new CompoundTag();
                            tag.putFloat("saturation_cost", saturationCost);
                            PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.DOUBLE_JUMP, tag));

                            player.jumpFromGround();

                            runtimeData.canDoubleJump = false;

                            int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                            GeneChipAPI.addChipCooldown(player, ChipTypes.DOUBLE_JUMP.get(), cooldown);
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event) {
        Player entity = event.getEntity();
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(entity);
        int fallFlyingTicks = (int) entity.fallDistance;
        if (fallFlyingTicks > 1 && !playerRunTimeData.isFalling) {
            playerRunTimeData.isFalling = true;
            AnimationHandler.playAnimation(entity, GeneChip.id("in_air_loop"));
        }
        if (playerRunTimeData.isFalling && entity.onGround()) {
            AnimationHandler.playAnimation(entity, null);
            playerRunTimeData.isFalling = false;
        }
    }
}
