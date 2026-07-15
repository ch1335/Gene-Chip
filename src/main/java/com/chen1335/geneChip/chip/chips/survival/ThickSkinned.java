package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

public class ThickSkinned extends Chip {

    private static final ResourceLocation ARMOR_ID = GeneChip.id("thick_skinned");

    public ThickSkinned() {
        super(makeTexture("thick_skinned"));
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (player.level().getGameTime() % 10 == 0) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 39, 0, false, false));
        }
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
        if (!playerRunTimeData.thickSkinnedActive && player.isShiftKeyDown()) {
            playerRunTimeData.thickSkinnedActive = true;
            addAttribute(player);
            serverPlayer.serverLevel().sendParticles(ParticleTypes.FIREWORK,
                    player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.4, 0.45, 0.4, 0.035);
        } else if (playerRunTimeData.thickSkinnedActive && !player.isShiftKeyDown()) {
            playerRunTimeData.thickSkinnedActive = false;
            removeAttribute(player);
        }

    }

    @Override
    public void onEquipped(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer)) return;
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
        if (player.isShiftKeyDown()) {
            playerRunTimeData.thickSkinnedActive = true;
            addAttribute(player);
        }
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
        if (playerRunTimeData.thickSkinnedActive) {
            playerRunTimeData.thickSkinnedActive = false;
            removeAttribute(player);
        }
    }

    private void addAttribute(Player player) {
        AttributeInstance attributeInstance = player.getAttributes().getInstance(Attributes.ARMOR);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(ARMOR_ID);
            attributeInstance.addTransientModifier(new AttributeModifier(ARMOR_ID, 4, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    private void removeAttribute(Player player) {
        AttributeInstance attributeInstance = player.getAttributes().getInstance(Attributes.ARMOR);
        if (attributeInstance != null) {
            attributeInstance.removeModifier(ARMOR_ID);
        }
    }
}
