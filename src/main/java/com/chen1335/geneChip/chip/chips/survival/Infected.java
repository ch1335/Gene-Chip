package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.immunity.data.InfectionZoneManager;
import com.immunity.util.ImmunityServerUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

public class Infected extends Chip {
    public final JsValueCalculator effectExtension = new JsValueCalculator("0.5", true);

    public Infected() {
        super(makeTexture("infected"));
        registerConfigValue("effect_extension", effectExtension);
    }

    @Override
    public ChipType getType() {
        return ChipType.SURVIVAL;
    }

    @Override
    public void tick(Player player, ChipInstance<?> instance) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        boolean wasInZone = runtimeData.infectedInZone;
        boolean inZone = isInInfectionZone(serverPlayer);
        runtimeData.infectedInZone = inZone;
        if (inZone && !wasInZone) {
            serverPlayer.serverLevel().sendParticles(ParticleTypes.FIREWORK,
                    player.getX(), player.getY() + 1.0, player.getZ(), 8, 0.45, 0.5, 0.45, 0.035);
        }

        if (inZone) {
            int strengthLevel = 1; // 力量 II (amplifier=1)

            // 血月联动：力量提升至III，但每分钟损失10点免疫力
            if (WorldFactorSynergy.isBloodMoon()) {
                strengthLevel = 2;
                if (player.level().getGameTime() % 120 == 0) {
                    ImmunityServerUtil.addImmunity(serverPlayer, -1);
                }
            }

            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 39, strengthLevel, false, false));

            // 感染溢出联动：攻击速度+15%
            if (WorldFactorSynergy.isInfectionOverflow()) {
                player.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 39, 0, false, false));
            }
        }
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        runtimeData.infectedInZone = false;
    }

    private boolean isInInfectionZone(ServerPlayer player) {
        InfectionZoneManager zoneManager = InfectionZoneManager.get(player.getServer());
        if (zoneManager == null) return false;
        return zoneManager.getDrain(player.serverLevel(), player.blockPosition()) > 0;
    }
}
