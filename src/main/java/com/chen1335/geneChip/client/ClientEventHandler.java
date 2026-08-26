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
import net.minecraft.world.phys.Vec3;
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
        ClientCardHudState.tick();
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
    public static void MouseInputEvent(InputEvent.MouseButton.Pre event) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || event.getAction() != InputConstants.PRESS) return;

        KeyMapping attackKey = Minecraft.getInstance().options.keyAttack;
        if (attackKey.getKey().getType() != InputConstants.Type.MOUSE
                || event.getButton() != attackKey.getKey().getValue()) {
            return;
        }

        GeneChipClient.getPlayerEquippedChip(ChipTypes.FLYING_KICK).ifPresent(chipInstance -> {
            if (!player.isSprinting() || player.onGround()
                    || GeneChipAPI.isChipCooldown(player, ChipTypes.FLYING_KICK.get())) {
                return;
            }

            float saturationCost = chipInstance.getChip().saturationCost.getValue(chipInstance.getLvl());
            if (player.getFoodData().getSaturationLevel() < saturationCost) return;

            PacketDistributor.sendToServer(new PlayerActionPacket(
                    PlayerActionPacket.ActionType.FLYING_KICK, new CompoundTag()));
            // 仅作输入侧节流，最终资源消耗与动作状态仍由服务端决定。
            GeneChipAPI.addChipCooldown(player, ChipTypes.FLYING_KICK.get(),
                    (int) (chipInstance.getChip().cooldown.getValue(chipInstance.getLvl()) * 20));
            AnimationHandler.playAnimationAndDistribute(
                    player, ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "flying_kick"));
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

                if (!GeneChipAPI.isChipCooldown(player, ChipTypes.TACTICAL_ROLL.get())
                        && !runtimeData.tacticalRolling && !ClientCardHudState.tacticalRolling) {
                    long currentTime = System.currentTimeMillis();
                    if (currentTime - lastBackKeyPress < DOUBLE_TAP_THRESHOLD) {
                        TacticalRoll chip = chipInstance.getChip();
                        float rollDistance = chip.rollDistance.getValue(chipInstance.getLvl()) * 0.7F;

                        player.setDeltaMovement(player.getViewVector(0).multiply(1, 0, 1).scale(-rollDistance));

                        PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.TACTICAL_ROLL, new CompoundTag()));

                        int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                        GeneChipAPI.addChipCooldown(player, ChipTypes.TACTICAL_ROLL.get(), cooldown);

                        AnimationHandler.playAnimationAndDistribute(player, ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "tactical_roll"));

                    }
                    lastBackKeyPress = currentTime;
                }
            });
        }

        if (event.getKey() == Minecraft.getInstance().options.keyJump.getKey().getValue()) {
            // 二段跳芯片逻辑
            GeneChipClient.getPlayerEquippedChip(ChipTypes.DOUBLE_JUMP).ifPresent(chipInstance -> {
                PlayerRunTimeData runtimeData = player.getData(GCAttachmentTypes.PLAYER_RUN_TIME_DATA);
                if (!player.onGround() && runtimeData.canDoubleJump && ClientCardHudState.canDoubleJump) {
                    if (!GeneChipAPI.isChipCooldown(player, ChipTypes.DOUBLE_JUMP.get())) {
                        DoubleJump chip = chipInstance.getChip();
                        float saturationCost = chip.saturationCost.getValue(chipInstance.getLvl());

                        if (player.getFoodData().getFoodLevel() >= saturationCost) {
                            CompoundTag tag = new CompoundTag();
                            tag.putFloat("saturation_cost", saturationCost);
                            PacketDistributor.sendToServer(new PlayerActionPacket(PlayerActionPacket.ActionType.DOUBLE_JUMP, tag));

                            player.addDeltaMovement(new Vec3(0,0.7,0));

                            runtimeData.canDoubleJump = false;

                            int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
                            GeneChipAPI.addChipCooldown(player, ChipTypes.DOUBLE_JUMP.get(), cooldown);
                            AnimationHandler.playAnimationAndDistribute(player, ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "double_jump"));

                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void PlayerTickEvent(PlayerTickEvent.Post event) {
        Player entity = event.getEntity();
        if (entity.isLocalPlayer()) {
            PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(entity);
            boolean isFalling = !entity.onGround() && (entity.getDeltaMovement().y <= -1);
            if (isFalling) {
                if (playerRunTimeData.fallingAnimationTick == 0) {
                    AnimationHandler.playAnimation(entity, GeneChip.id("falling_start"));
                } else if (playerRunTimeData.fallingAnimationTick == 3) {
                    AnimationHandler.playAnimation(entity, GeneChip.id("falling"));
                }
                playerRunTimeData.fallingAnimationTick++;
            } else {
                if (playerRunTimeData.fallingAnimationTick != 0) {
                    AnimationHandler.playAnimation(entity, GeneChip.id("falling_end"));
                }
                playerRunTimeData.fallingAnimationTick = 0;
            }
        }
    }
}
