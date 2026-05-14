package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class Photosynthesis extends Chip {
    private static final ResourceLocation SPEED_ID = GeneChip.id("photosynthesis_speed");

    public Photosynthesis() {
        super(makeTexture("photosynthesis"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
        playerRunTimeData.photosynthesisTimer++;

        if (playerRunTimeData.photosynthesisTimer >= 30 * 20) {
            playerRunTimeData.photosynthesisTimer = 0;

            if (isUnderDirectSunlight(player)) {
                if (playerRunTimeData.photosynthesisStacks < 5) {
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
                player.getFoodData().eat(2, 0);

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

        AttributeInstance attributeInstance = player.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(SPEED_ID);
        }
    }
}
