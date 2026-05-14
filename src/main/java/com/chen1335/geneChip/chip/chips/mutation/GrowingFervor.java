package com.chen1335.geneChip.chip.chips.mutation;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class GrowingFervor extends Chip {
    private static final ResourceLocation ATTACK_SPEED = GeneChip.id("growing_fervor_attack_speed");
    private static final ResourceLocation MOVE_SPEED = GeneChip.id("growing_fervor_move_speed");

    public GrowingFervor() {
        super(makeTexture("growing_fervor"));
    }

    @Override
    public ChipType getType() {
        return ChipType.MUTATION;
    }

    @Override
    public void onEquipped(Player player, ChipInstance<?> instance) {
        onImmunityValueChanged(player, instance, GeneChipAPI.getImmunityValue(player));
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        AttributeMap attributes = player.getAttributes();
        AttributeInstance attackSpeed = attributes.getInstance(Attributes.ATTACK_SPEED);
        AttributeInstance moveSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (moveSpeed == null || attackSpeed == null) {
            return;
        }
        attackSpeed.removeModifier(ATTACK_SPEED);
        moveSpeed.removeModifier(MOVE_SPEED);
    }

    @Override
    public void onImmunityValueChanged(Player player, ChipInstance<?> instance, int immunityValue) {
        AttributeMap attributes = player.getAttributes();
        AttributeInstance attackSpeed = attributes.getInstance(Attributes.ATTACK_SPEED);
        AttributeInstance moveSpeed = attributes.getInstance(Attributes.MOVEMENT_SPEED);
        if (moveSpeed == null || attackSpeed == null) {
            return;
        }
        attackSpeed.removeModifier(ATTACK_SPEED);
        moveSpeed.removeModifier(MOVE_SPEED);

        if (immunityValue < 25) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            moveSpeed.addTransientModifier(new AttributeModifier(MOVE_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (immunityValue < 50) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            moveSpeed.addTransientModifier(new AttributeModifier(MOVE_SPEED, 0.1, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        } else if (immunityValue < 75) {
            attackSpeed.addTransientModifier(new AttributeModifier(ATTACK_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
            moveSpeed.addTransientModifier(new AttributeModifier(MOVE_SPEED, 0.05, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        }


    }
}
