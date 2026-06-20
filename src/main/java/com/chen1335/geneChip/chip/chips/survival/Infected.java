package com.chen1335.geneChip.chip.chips.survival;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.immunity.data.InfectionZoneManager;
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
        boolean inZone = isInInfectionZone(serverPlayer);
        runtimeData.infectedInZone = inZone;

        if (inZone) {
            // 处于感染区时给予力量 II
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 39, 1, false, false));
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
