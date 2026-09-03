package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class Photosynthesis extends Chip {
    private static final ResourceLocation SPEED_ID = GeneChip.id("photosynthesis_speed");
    private static final int BASE_INTERVAL = 30 * 20;
    private static final int BASE_MAX_STACKS = 5;

    public Photosynthesis() {
        super(makeTexture("photosynthesis"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (player.level().isClientSide) return;
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);

        int interval = BASE_INTERVAL;
        int maxStacks = BASE_MAX_STACKS;

        // 阳光裂隙联动：充能速度x3，上限8层
        if (WorldFactorSynergy.isRaysOfSunlight()) {
            interval = BASE_INTERVAL / 3;
            maxStacks = 8;
        } else if (WorldFactorSynergy.isFineWeather()) {
            // 天气晴朗联动：充能速度x2
            interval = BASE_INTERVAL / 2;
        }

        playerRunTimeData.photosynthesisInterval = interval;
        playerRunTimeData.photosynthesisMaxStacks = maxStacks;
        playerRunTimeData.photosynthesisCharging = isUnderDirectSunlight(player);
        playerRunTimeData.photosynthesisTimer++;

        if (playerRunTimeData.photosynthesisTimer >= interval) {
            playerRunTimeData.photosynthesisTimer = 0;

            if (playerRunTimeData.photosynthesisCharging) {
                if (playerRunTimeData.photosynthesisStacks < maxStacks) {
                    playerRunTimeData.photosynthesisStacks++;
                    updateMovementSpeed(player, playerRunTimeData.photosynthesisStacks);
                }
            } else {
                if (playerRunTimeData.photosynthesisStacks > 0) {
                    playerRunTimeData.photosynthesisStacks--;
                    updateMovementSpeed(player, playerRunTimeData.photosynthesisStacks);
                }
            }

            if (playerRunTimeData.photosynthesisStacks > 0) {
                GeneChipAPI.consumeFood(player,2,instance);
            }
        }
    }

    private boolean isUnderDirectSunlight(Player player) {
        return player.level().isDay()
                && player.level().canSeeSky(player.blockPosition())
                && !player.level().isRaining()
                && !player.level().isThundering();
    }

    private void updateMovementSpeed(Player player, int stacks) {
        AttributeInstance attributeInstance = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(SPEED_ID);
            if (stacks > 0) {
                double speedBonus = stacks * 0.01;
                attributeInstance.addTransientModifier(new AttributeModifier(SPEED_ID, speedBonus, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
            }
        }
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
        playerRunTimeData.photosynthesisStacks = 0;
        playerRunTimeData.photosynthesisTimer = 0;
        playerRunTimeData.photosynthesisCharging = false;

        AttributeInstance attributeInstance = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(SPEED_ID);
        }
    }
}
